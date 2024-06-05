package erythcore.frontend.InstFetch

import chisel3._
import chisel3.util._
import erythcore._

import bus.ivybus._

class IFU extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val mem_ports = new IvyBus

        val out = Decoupled(new InstFetchIO)
    })

    // FSM
    val sIDLE :: sREQ :: sRECV :: Nil = Enum(3)
    val state = RegInit(sIDLE)
    switch (state){
        is (sIDLE){
            when (~reset.asBool){
                state := sREQ
            }
        }
        is (sREQ){
            when (io.mem_ports.req.fire){
                state := sRECV
            }
        }
        is (sRECV){
            when (io.mem_ports.resp.fire){
                state := sIDLE
            }
        }
    }

    // pc
    // TODO: add bpu redirect
    val pc      = RegInit(ErythSetting.RESETVEC.U(XLEN.W))
    val pnpc    = pc + 4.U
    when (io.out.fire){
        pc := pnpc
    }

    // Req
    io.mem_ports.req.valid  := state === sREQ
    io.mem_ports.req.bits.wen   := 0.B
    io.mem_ports.req.bits.addr  := pc
    io.mem_ports.req.bits.mask  := 0.U
    io.mem_ports.req.bits.data  := 0.U
    io.mem_ports.req.bits.size  := "b010".U

    // Resp
    val has_resp_fire   = RegInit(0.B)
    when (io.mem_ports.resp.fire & ~io.out.fire){
        has_resp_fire   := 1.B
    }.elsewhen(io.out.fire){
        has_resp_fire   := 0.B
    }

    io.mem_ports.resp.ready := state === sRECV
    val data_r  = RegEnable(io.mem_ports.resp.bits.data, io.mem_ports.resp.fire)
    val inst    = Mux(has_resp_fire, data_r, io.mem_ports.resp.bits.data)

    // to IDU
    val ifu_out = Wire(new InstFetchIO)
    ifu_out.instValid   := ~reset.asBool
    ifu_out.instr       := inst
    ifu_out.pc          := pc
    ifu_out.pnpc        := pnpc

    io.out.valid    := io.mem_ports.resp.fire | has_resp_fire
    io.out.bits     := ifu_out
}
package bus.ivybus

// Convertor!
import chisel3._
import chisel3.util._
import bus.axi4.AXI4Lite
import erythcore.ErythrinaDefault
import utils.LookupTreeDefault

// convert a Ivy request to AXI4
class Ivy2AXI4Lite extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val in  = Flipped(new IvyBus)
        val out = new AXI4Lite
    })

    // FSM
    val sARW :: sR :: sW :: sB ::Nil = Enum(4)
    val state   = RegInit(sARW)
    switch (state){
        is (sARW){
            when (io.in.req.bits.wen & io.out.aw.fire){
                state   := sW
            }.elsewhen(~io.in.req.bits.wen & io.out.ar.fire){
                state   := sR
            }
        }
        is (sR){
            when (io.out.r.fire & io.in.resp.fire){
                state   := sARW
            }
        }
        is (sW){
            when (io.out.w.fire){
                state   := sB
            }
        }
        is (sB){
            when (io.out.b.fire & io.in.resp.fire){
                state   := sARW
            }
        }

    }

    // AXI-Read
    io.out.ar.valid         := io.in.req.valid & ~io.in.req.bits.wen & state === sARW
    io.out.ar.bits.addr     := io.in.req.bits.addr

    io.out.r.ready          := io.in.resp.ready & state === sR

    // AXI-Write
    io.out.aw.valid         := io.in.req.valid & io.in.req.bits.wen & state === sARW
    io.out.aw.bits.addr     := io.in.req.bits.addr

    val w_data_r    = RegEnable(io.in.req.bits.data, io.out.aw.fire)
    val w_strb_r    = RegEnable(io.in.req.bits.mask, io.out.aw.fire)
    io.out.w.ready          := state === sW
    io.out.w.bits.data      := w_data_r
    io.out.w.bits.strb      := w_strb_r

    io.out.b.ready          := io.in.resp.ready & state === sB

    // IvyBus
    io.in.req.ready         := state === sARW
    io.in.resp.valid        := (state === sR && io.out.r.valid) | (state === sB && io.out.b.valid)
    io.in.resp.bits.data    := io.out.r.bits.data
    io.in.resp.bits.rsp     := Mux(state === sB, io.out.b.bits.resp, io.out.r.bits.resp)
}

// convert a AXI request to Ivy request
class AXI4Lite2Ivy extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val in  = Flipped(new AXI4Lite)
        val out = new IvyBus
    })

    // FSM
    val sARW :: sR :: sW :: sB ::Nil = Enum(4)
    val state   = RegInit(sARW)

    switch (state){
        is (sARW){
            when (io.in.ar.fire){
                state   := sR
            }.elsewhen(io.in.aw.fire){
                state   := sW
            }
        }
        is (sR){
            when (io.in.r.fire){
                state   := sARW
            }
        }
        is (sW){
            when (io.in.w.fire){
                state   := sB
            }
        }
        is (sB){
            when (io.in.b.fire){
                state   := sARW
            }
        }
    }

    // AXI-Read
    io.in.ar.ready      := state === sARW & io.out.req.ready

    io.in.r.valid       := state === sR & io.out.resp.valid
    io.in.r.bits.data   := io.out.resp.bits.data
    io.in.r.bits.resp   := io.out.resp.bits.rsp

    // AXI-Write
    io.in.aw.ready      := state === sARW

    io.in.w.ready       := state === sW & io.out.req.ready

    val w_resp_r    = RegEnable(io.out.resp.bits.rsp, io.in.w.fire)
    io.in.b.valid       := state === sB
    io.in.b.bits.resp   := w_resp_r

    // IvyBus
    io.out.req.valid        := (state === sARW & io.in.ar.valid) | (state === sW & io.in.w.valid)
    io.out.req.bits.wen     := state === sW
    
    val w_addr_r    = RegEnable(io.in.aw.bits.addr, io.in.aw.fire)
    io.out.req.bits.addr    := Mux(state === sARW, io.in.ar.bits.addr, w_addr_r)
    io.out.req.bits.data    := io.in.w.bits.data
    io.out.req.bits.mask    := io.in.w.bits.strb

    io.out.resp.ready       := LookupTreeDefault(state, 0.B, List(
        sW  -> io.in.w.ready,
        sR  -> io.in.r.ready
    ))
}
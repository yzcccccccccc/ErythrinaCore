package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import bus.ivybus._

import utils._

// Instruction Fetch Stage (pipeline, to be continued)

class IFUIO extends Bundle with IFUtrait{
  val ifu_to_idu = Decoupled(new if_to_id_zip)         // pipeline ctrl, to IDU
  val bpu_to_ifu = Flipped(new RedirectInfo)
  val ifu_mem = new IvyBus

  // perf
  val ifu_perf_probe = Flipped(new PerfIFU)
}

class IFU extends Module with IFUtrait{
  val io = IO(new IFUIO)

  val sIDLE :: sREQ :: sRECV :: Nil = Enum(3)
  val state = RegInit(sIDLE)
  switch (state){
    is (sIDLE){
      when (~reset.asBool){
        state := sREQ
      }
    }
    is (sREQ){
      when (io.ifu_mem.req.fire){
        state := sRECV
      }
    }
    is (sRECV){
      when (io.ifu_mem.resp.fire){
        state := sIDLE
      }
    }
  }

  // flush (bpu)
  val flush   = (io.ifu_mem.req.fire | state === sRECV) & io.bpu_to_ifu.redirect
  val flush_r = Reg(Bool())
  when (io.ifu_mem.req.fire | state === sRECV){
    flush_r := io.bpu_to_ifu.redirect
  }.elsewhen(io.ifu_mem.resp.fire){
    flush_r := 0.B
  }

  // pc
  val pc    = RegInit(ErythrinaSetting.RESETVEC.U(XLEN.W))
  val snpc  = pc + 4.U
  when (io.bpu_to_ifu.redirect){
    pc := io.bpu_to_ifu.target
  }.elsewhen(io.ifu_to_idu.fire){
    pc := snpc
  }
  
  io.ifu_mem.req.valid      := state === sREQ
  io.ifu_mem.req.bits.wen   := 0.B
  io.ifu_mem.req.bits.addr  := pc
  io.ifu_mem.req.bits.mask  := 0.U
  io.ifu_mem.req.bits.data  := 0.U
  io.ifu_mem.req.bits.size  := "b010".U     // 4 bytes transfer

  // inst
  io.ifu_mem.resp.ready   := state === sRECV
  val inst    = io.ifu_mem.resp.bits.data

  // IFU to IDU zip
  val inst_valid = Reg(Bool())

  io.ifu_to_idu.valid               := io.ifu_mem.resp.fire
  io.ifu_to_idu.bits.content_valid  := ~flush_r
  io.ifu_to_idu.bits.pc             := pc
  io.ifu_to_idu.bits.inst           := inst


  // perf
  io.ifu_perf_probe.get_inst_event := io.ifu_mem.resp.fire
  io.ifu_perf_probe.wait_req_event := state === sREQ
  io.ifu_perf_probe.wait_resp_event := state === sRECV
}
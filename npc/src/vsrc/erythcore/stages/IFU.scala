package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import bus.ivybus._

import utils._

// Instruction Fetch Stage (pipeline, to be continued)

class IFUIO extends Bundle with IFUtrait{
  val ifu_idu_zip   = Decoupled(new IF_ID_zip)         // pipeline ctrl, to IDU
  val bpu_redirect  = Flipped(new RedirectInfo)
  val ifu_mem = new IvyBus

  // perf
  val ifu_perf_probe = Flipped(new PerfIFU)
}

class IFU extends Module with IFUtrait{
  val io = IO(new IFUIO)

  val has_resp_fire = RegInit(false.B)
  when (io.ifu_mem.resp.fire & ~io.ifu_idu_zip.fire){
    has_resp_fire := true.B
  }.elsewhen(io.ifu_idu_zip.fire){
    has_resp_fire := false.B
  }

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
      when ((io.ifu_mem.resp.fire | has_resp_fire) & io.ifu_idu_zip.fire){
        state := sREQ
      }
    }
  }

  // flush (bpu)
  val flush   = (io.ifu_mem.req.fire | state === sRECV) & io.bpu_redirect.redirect
  val flush_r = Reg(Bool())
  when (io.ifu_mem.req.fire | state === sRECV){
    flush_r := io.bpu_redirect.redirect
  }.elsewhen(io.ifu_idu_zip.fire){
    flush_r := 0.B
  }

  // pc
  val pc    = RegInit(ErythrinaSetting.RESETVEC.U(XLEN.W))
  val snpc  = pc + 4.U
  when (io.bpu_redirect.redirect){
    pc := io.bpu_redirect.target
  }.elsewhen(io. ifu_idu_zip.fire & ~flush_r){
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
  val data_r  = RegEnable(io.ifu_mem.resp.bits.data, io.ifu_mem.resp.fire)
  val inst    = Mux(has_resp_fire, data_r, io.ifu_mem.resp.bits.data)

  // IFU to IDU zip
  val content_valid = ~flush_r & ~reset.asBool
  io. ifu_idu_zip.valid               := io.ifu_mem.resp.fire | has_resp_fire
  io. ifu_idu_zip.bits.content_valid  := ~flush_r & ~reset.asBool
  io. ifu_idu_zip.bits.pc             := pc
  io. ifu_idu_zip.bits.inst           := inst


  // perf
  io.ifu_perf_probe.get_inst_event := io.ifu_mem.resp.fire
  io.ifu_perf_probe.wait_req_event := state === sREQ
  io.ifu_perf_probe.wait_resp_event := state === sRECV
}
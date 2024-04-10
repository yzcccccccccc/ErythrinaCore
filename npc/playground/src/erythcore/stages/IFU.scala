package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import bus.ivybus._

// Instruction Fetch Stage (pipeline, to be continued)

class IFUIO extends Bundle with IFUtrait{
  val step    = Input(Bool())
  val req_en  = Input(Bool())
  val IFU2IDU = Decoupled(new IF2IDzip)         // pipeline ctrl, to IDU
  val BPU2IFU = Flipped(new RedirectInfo)
  val ifu_mem = new IvyBus
}

class IFU extends Module with IFUtrait{
  val io = IO(new IFUIO)

  // pc
  val pc    = RegInit(RESETVEC.U)
  val snpc  = pc + 4.U
  when (io.step){
    when (io.BPU2IFU.redirect){
      pc := io.BPU2IFU.target
    }.otherwise{
      pc := snpc
    }
  }
  
  val valid_r = Reg(Bool())
  valid_r := ~reset.asBool
  io.ifu_mem.req.valid      := valid_r & io.req_en
  io.ifu_mem.req.bits.wen   := 0.B
  io.ifu_mem.req.bits.addr  := pc
  io.ifu_mem.req.bits.mask  := 0.U
  io.ifu_mem.req.bits.data  := 0.U

  // inst
  io.ifu_mem.resp.ready   := 1.B
  //io.IFU_memResp.ready  := 1.B
  val inst    = io.ifu_mem.resp.bits.data
  val inst_r  = RegEnable(io.ifu_mem.resp.bits.data, io.ifu_mem.resp.fire)

  // zip
  val inst_valid = Reg(Bool())
  when (io.ifu_mem.resp.fire){
    inst_valid  := 1.B
  }.elsewhen(io.step){
    inst_valid  := 0.B
  }
  io.IFU2IDU.valid       := inst_valid
  io.IFU2IDU.bits.inst   := Mux(inst_valid, inst_r, inst)
  io.IFU2IDU.bits.pc     := pc
}
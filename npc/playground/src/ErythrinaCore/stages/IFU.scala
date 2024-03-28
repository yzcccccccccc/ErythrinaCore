package ErythrinaCore

import chisel3._
import chisel3.util._

import bus.mem._

// Instruction Fetch Stage (pipeline, to be continued)

class IFUIO extends Bundle with IFUtrait{
  val step    = Input(Bool())
  val IFU2IDU = Decoupled(new IF2IDzip)         // pipeline ctrl, to IDU
  val BPU2IFU = Flipped(new RedirectInfo)
  val IFU_memReq  = Decoupled(new MemReqIO)
  val IFU_memResp = Flipped(Decoupled(new MemRespIO))
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
  io.IFU_memReq.valid     := valid_r
  io.IFU_memReq.bits.addr := pc
  io.IFU_memReq.bits.mask := 0.U
  io.IFU_memReq.bits.data := 0.U


  // inst
  io.IFU_memResp.ready  := 1.B
  val inst = io.IFU_memResp.bits.data

  // zip
  val inst_valid = Reg(Bool())
  when (io.IFU_memResp.fire){
    inst_valid  := 1.B
  }.elsewhen(io.step){
    inst_valid  := 0.B
  }
  io.IFU2IDU.valid       := inst_valid
  io.IFU2IDU.bits.inst   := inst
  io.IFU2IDU.bits.pc     := pc
}
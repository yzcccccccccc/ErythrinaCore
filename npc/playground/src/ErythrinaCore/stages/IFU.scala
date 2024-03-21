package ErythrinaCore

import chisel3._
import chisel3.util._

import bus.mem._

// Instruction Fetch Stage (pipeline, to be continued)

class IFUIO extends Bundle with IFUtrait{
  val IFU2IDU = Decoupled(new IF2IDzip)         // pipeline ctrl, to IDU
  val IFU_memReq  = Decoupled(new MemReqIO)
  val IFU_memResp = Flipped(Decoupled(new MemRespIO))
}

class IFU extends Module with IFUtrait{
  val io = IO(new IFUIO)

  // pc
  val pc    = RegInit(RESETVEC.U)
  val snpc  = pc + 4.U
  pc := snpc
  io.IFU_memReq.valid     := 1.B
  io.IFU_memReq.bits.addr := pc


  // inst
  io.IFU_memResp.ready  := 1.B
  val inst = io.IFU_memResp.bits.data

  // zip
  io.IFU2IDU.valid       := 1.B
  io.IFU2IDU.bits.inst   := inst
  io.IFU2IDU.bits.pc     := pc
}
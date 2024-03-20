package ErythrinaCore

import chisel3._
import chisel3.util._

// Instruction Fetch Stage (pipeline, to be continued)

class IFUIO extends Bundle with IFUtrait{
  val IFU2IDU = Decoupled(new IF2IDzip)         // pipeline ctrl, to IDU
  val inst_addr = Output(UInt(XLEN.W))        // to be continued
  val inst_data = Input(UInt(XLEN.W))
}

class IFU extends Module with IFUtrait{
  val io = IO(new IFUIO)

  // pc
  val pc  = RegInit(RESETVEC.U)
  pc := pc + 4.U
  io.inst_addr := pc

  // inst
  val inst = io.inst_data

  // zip
  io.IFU2IDU.valid  := true.B
  io.IFU2IDU.bits.inst   := inst
  io.IFU2IDU.bits.pc     := pc
}
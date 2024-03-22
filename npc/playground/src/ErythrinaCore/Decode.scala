package ErythrinaCore

import chisel3._
import chisel3.util._

// Instruction Type
trait InstrType extends ErythrinaDefault{
  def TypeI = "b000".U
  def TypeR = "b001".U
  def TypeS = "b010".U
  def TypeU = "b011".U
  def TypeJ = "b100".U
  def TypeB = "b101".U
  def TypeN = "b110".U
}

object SrcType {
  def reg = "b00".U
  def pc  = "b01".U
  def imm = "b11".U
}

object Instructions extends InstrType{
  val decodeDefault = List(TypeN, ALUop.nop, LSUop.nop, BPUop.nop)
  def decode_table = RV32I.table
}
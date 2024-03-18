package ErithrinaCore

import chisel3._
import chisel3.util._

// ALU: for Arithmetic Operations

trait ALUtrait extends ErythrinaDefault{
  val ALUopLEN = 3
}

object ALUop{
  def add   = "b000".U
  def sub   = "b001".U
  def slt   = "b010".U
  def sltu  = "b011".U
  def and   = "b100".U
  def or    = "b101".U
  def xor   = "b110".U
  def nor   = "b111".U

  def usesub(aluop: UInt) = ~aluop(2) & (aluop(1,0) =/= 0.U)
}

class ALUIO extends Bundle with ALUtrait{
  val src1 = Input(UInt(XLEN.W))
  val src2 = Input(UInt(XLEN.W))
  val aluop = Input(UInt(ALUopLEN.W))
  val res = Output(UInt(XLEN.W))
}

class ALU extends Module with ALUtrait{
  val io = IO(new ALUIO)

  val (src1, src2, aluop) = (io.src1, io.src2, io.aluop)

  val UseSub = ALUop.usesub(aluop)
  val shamt = src2(4, 0)

  val add_sub_res = (src1 +& (src2 ^ Fill(XLEN, UseSub))) + UseSub

  io.res := add_sub_res

}
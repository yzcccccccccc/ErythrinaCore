package ErythrinaCore

import chisel3._
import chisel3.util._

// ALU: for Arithmetic Operations

trait ALUtrait extends ErythrinaDefault{
  val ALUopLEN = 4
}

object ALUop{
  def dir   = "b1001".U     // direct, for lui, jalr etc. (result in a reg write :D)
  def nop   = "b1000".U     // don't use alu (wfi etc.)
  def add   = "b0000".U
  def sub   = "b0001".U
  def slt   = "b0010".U
  def sltu  = "b0011".U
  def and   = "b0100".U
  def or    = "b0101".U
  def xor   = "b0110".U
  def nor   = "b0111".U
  def srl   = "b1010".U
  def sra   = "b1011".U
  def sll   = "b1100".U

  def usesub(aluop: UInt) = (aluop(3,2) === 0.U) & (aluop(1,0) =/= 0.U)
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
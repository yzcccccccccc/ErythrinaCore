package erythcore

import chisel3._
import chisel3.util._
import utils.LookupTree
import utils.LookupTreeDefault
import utils._

// ALU: for Arithmetic Operations

trait ALUtrait extends ErythrinaDefault{
  val ALUopLEN = 5
}

object ALUop{
  def dir   = "b01001".U     // direct, for lui, jalr etc. (result in a reg write :D)
  def nop   = "b01000".U     // don't use alu (wfi etc.)
  def add   = "b00000".U
  def sub   = "b00001".U
  def slt   = "b00010".U
  def sltu  = "b00011".U
  def and   = "b00100".U
  def or    = "b00101".U
  def xor   = "b00110".U
  def srl   = "b01010".U
  def sra   = "b01011".U
  def sll   = "b01100".U

  def mul     = "b10000".U
  def mulh    = "b10001".U
  def mulhsu  = "b10010".U
  def mulhu   = "b10011".U

  def usesub(aluop: UInt) = (aluop(3,2) === 0.U) & (aluop(1,0) =/= 0.U)
  def usemul(aluop: UInt) = aluop(4) & ~aluop(2)
  def usediv(aluop: UInt) = aluop(4) & aluop(2)
}

class ALUIO_in extends Bundle with ALUtrait{
  val src1 = Input(UInt(XLEN.W))
  val src2 = Input(UInt(XLEN.W))
  val aluop = Input(UInt(ALUopLEN.W))
}

class ALUIO_out extends Bundle with ALUtrait{
  val zero  = Output(Bool())
  val res   = Output(UInt(XLEN.W))
}

class ALUIO extends Bundle with ALUtrait{
  val ALUin   = new ALUIO_in
  val ALUout  = new ALUIO_out
}

class ALU extends Module with ALUtrait{
  val io = IO(new ALUIO)

  val (src1, src2, aluop) = (io.ALUin.src1, io.ALUin.src2, io.ALUin.aluop)

  val UseSub  = ALUop.usesub(aluop)
  val shamt   = src2(4, 0)
  val src2in  = src2 ^ Fill(XLEN, UseSub)

  val add_sub_res = (src1 +& src2in) + UseSub
  val sltu_res  = ~add_sub_res(XLEN)
  val overflow  = (src1(XLEN-1) & src2in(XLEN-1) & ~add_sub_res(XLEN-1)) | (~src1(XLEN-1) & ~src2in(XLEN-1) & add_sub_res(XLEN-1))
  val slt_res   = overflow ^ add_sub_res(XLEN-1)

  val res = LookupTreeDefault(aluop, add_sub_res, List(
    ALUop.dir   -> src1,
    ALUop.slt   -> ZeroExt(slt_res, XLEN),
    ALUop.sltu  -> ZeroExt(sltu_res, XLEN),
    ALUop.and   -> (src1 & src2),
    ALUop.xor   -> (src1 ^ src2),
    ALUop.or    -> (src1 | src2),
    ALUop.srl   -> (src1 >> shamt),
    ALUop.sra   -> (SignExt(src1, 2 * XLEN) >> shamt)(XLEN - 1, 0),
    ALUop.sll   -> (src1 << shamt)(XLEN-1, 0)
  ))

  io.ALUout.res   := res
  io.ALUout.zero  := (io.ALUout.res === 0.U)
}
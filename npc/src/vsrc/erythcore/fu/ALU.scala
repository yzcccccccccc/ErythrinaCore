package erythcore

import chisel3._
import chisel3.util._
import utils.LookupTree
import utils.LookupTreeDefault
import utils._
import erythcore.fu.mul.Multiplier

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

  def div     = "b10100".U
  def divu    = "b10101".U
  def rem     = "b10110".U
  def remu    = "b10111".U

  def usesub(aluop: UInt) = (aluop(3,2) === 0.U) & (aluop(1,0) =/= 0.U)
  def usemul(aluop: UInt) = aluop(4) & ~aluop(2)
  def usediv(aluop: UInt) = aluop(4) & aluop(2)
}

class ALUIO_in extends Bundle with ALUtrait{
  val flush = Input(Bool())
  val src1  = Input(UInt(XLEN.W))
  val src2  = Input(UInt(XLEN.W))
  val aluop = Input(UInt(ALUopLEN.W))
}

class ALUIO_out extends Bundle with ALUtrait{
  val zero  = Output(Bool())
  val res   = Output(UInt(XLEN.W))
}

class ALUIO extends Bundle with ALUtrait{
  val ALUin   = new ALUIO_in
  val ALUout  = Decoupled(new ALUIO_out)
}

class ALU extends Module with ALUtrait{
  val io = IO(new ALUIO)

  val (src1, src2, aluop) = (io.ALUin.src1, io.ALUin.src2, io.ALUin.aluop)

  /* ---------- Normal ALU ---------- */
  val UseSub  = ALUop.usesub(aluop)
  val shamt   = src2(4, 0)
  val src2in  = src2 ^ Fill(XLEN, UseSub)

  val add_sub_res = (src1 +& src2in) + UseSub
  val sltu_res  = ~add_sub_res(XLEN)
  val overflow  = (src1(XLEN-1) & src2in(XLEN-1) & ~add_sub_res(XLEN-1)) | (~src1(XLEN-1) & ~src2in(XLEN-1) & add_sub_res(XLEN-1))
  val slt_res   = overflow ^ add_sub_res(XLEN-1)

  /* ---------- Multiplier ---------- */
  val isMul = ALUop.usemul(aluop)
  // FSM
  val sIDLE :: sENC :: sCAL :: sWAIT :: Nil = Enum(4)
  val state = RegInit(sIDLE)
  switch (state){
    is (sIDLE){
      when (isMul & ~io.ALUin.flush){
        state := sENC
      }
    }
    is (sENC){
      state := Mux(io.ALUin.flush, sIDLE, sCAL)
    }
    is (sCAL){
      state := Mux(io.ALUout.fire | io.ALUin.flush, sIDLE, sWAIT)
    }
  }

  val mul_inst = Module(new Multiplier)
  mul_inst.io.in_valid  := state === sIDLE & isMul
  mul_inst.io.a         := src1
  mul_inst.io.b         := src2
  mul_inst.io.op        := aluop(1, 0)
  val mul_valid   = mul_inst.io.res_valid | state === sWAIT | io.ALUin.flush
  val mul_res_r   = RegEnable(mul_inst.io.res, mul_inst.io.res_valid)
  val mul_res     = Mux(state === sWAIT, mul_res_r, mul_inst.io.res)

  /* ---------- Select Res ---------- */
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

  io.ALUout.valid     := Mux(isMul, mul_valid, true.B)
  io.ALUout.bits.res  := Mux(isMul, mul_res, res)
  io.ALUout.bits.zero := (io.ALUout.bits.res === 0.U)
}
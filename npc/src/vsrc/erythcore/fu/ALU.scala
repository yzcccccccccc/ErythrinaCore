package erythcore

import chisel3._
import chisel3.util._
import utils.LookupTree
import utils.LookupTreeDefault
import utils._
import erythcore.fu.mul.Multiplier
import erythcore.fu.div.Divisor

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
  val sM_IDLE :: sM_ENC :: sM_CAL :: sM_WAIT :: Nil = Enum(4)
  val mul_state = RegInit(sM_IDLE)
  switch (mul_state){
    is (sM_IDLE){
      when (isMul & ~io.ALUin.flush){
        mul_state := sM_ENC
      }
    }
    is (sM_ENC){
      mul_state := Mux(io.ALUin.flush, sM_IDLE, sM_CAL)
    }
    is (sM_CAL){
      mul_state := Mux(io.ALUin.flush, sM_IDLE, sM_WAIT)
    }
    is (sM_WAIT){
      when (io.ALUout.fire | io.ALUin.flush){
        mul_state := sM_IDLE
      }
    }
  }

  val mul_inst = Module(new Multiplier)
  mul_inst.io.in_valid  := mul_state === sM_IDLE & isMul
  mul_inst.io.a         := src1
  mul_inst.io.b         := src2
  mul_inst.io.op        := aluop(1, 0)
  val mul_valid   = mul_state === sM_WAIT | io.ALUin.flush
  val mul_res_r   = RegEnable(mul_inst.io.res, mul_inst.io.res_valid)
  val mul_res     = mul_res_r

  /* ---------- Divider ---------- */
  val isDiv = ALUop.usediv(aluop)
  // FSM
  val sD_IDLE :: sD_WORK :: sD_WAIT :: Nil = Enum(3)
  val div_state = RegInit(sD_IDLE)
  switch (div_state){
    is (sD_IDLE){
      when (isDiv & ~io.ALUin.flush){
        div_state := sD_WORK
      }
    }
    is (sD_WORK){
      div_state := Mux(io.ALUout.fire | io.ALUin.flush, sD_WAIT, sD_IDLE)
    }
    is (sD_WAIT){
      when (io.ALUout.fire | io.ALUin.flush){
        div_state := sD_IDLE
      }
    }
  }

  val div_inst = Module(new Divisor)
  div_inst.io.in_valid  := div_state === sD_IDLE & isDiv
  div_inst.io.in_flush  := io.ALUin.flush
  div_inst.io.a         := src1
  div_inst.io.b         := src2
  div_inst.io.op        := aluop(1, 0)
  val div_valid   = div_inst.io.res_valid | div_state === sD_WAIT | io.ALUin.flush
  val div_res_r   = RegEnable(div_inst.io.res, div_inst.io.res_valid)
  val div_res     = Mux(div_state === sD_WAIT, div_res_r, div_inst.io.res)

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

  io.ALUout.valid     := Mux1H(Seq(
    isMul -> mul_valid,
    isDiv -> div_valid,
    (~isMul & ~isDiv) -> 1.B
  ))
  io.ALUout.bits.res  := Mux1H(Seq(
    isMul -> mul_res,
    isDiv -> div_res,
    (~isMul & ~isDiv) -> res
  ))
  io.ALUout.bits.zero := (io.ALUout.bits.res === 0.U)
}
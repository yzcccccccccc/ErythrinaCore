package erythcore.backend.fu

import chisel3._
import chisel3.util._
import erythcore._
import utils._

object ALUOpType{
    def nop  = "b00000".U
    def add  = "b00001".U
    def sub  = "b10000".U
    def slt  = "b10001".U
    def sltu = "b10010".U
    def and  = "b00100".U
    def or   = "b00101".U
    def xor  = "b00110".U
    def sll  = "b00111".U
    def srl  = "b01000".U
    def sra  = "b01001".U

    def usesub(aluop:UInt):Bool = aluop(4)
}

class ALU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val src1    = Input(UInt(XLEN.W))
        val src2    = Input(UInt(XLEN.W))
        val aluop   = Input(FuOpType())

        val res     = Output(UInt(XLEN.W))
    })

    val (src1, src2, aluop) = (io.src1, io.src2, io.aluop)

    val usesub  = ALUOpType.usesub(aluop)
    val shamt   = src2(4, 0)
    val src2in  = src2 ^ Fill(XLEN, usesub)

    val add_sub_res = (src1 +& src2) + usesub
    val overflow    = (src1(XLEN - 1) & src2in(XLEN - 1) & ~add_sub_res(XLEN - 1)) | (~src1(XLEN - 1) & ~src2in(XLEN - 1) & add_sub_res(XLEN - 1))
    val sltu_res    = ~add_sub_res(XLEN)
    val slt_res     = overflow ^ add_sub_res(XLEN - 1)

    val res = LookupTreeDefault(aluop, add_sub_res, List(
        ALUOpType.slt   -> ZeroExt(slt_res, XLEN),
        ALUOpType.sltu  -> ZeroExt(sltu_res, XLEN),
        ALUOpType.and   -> (src1 & src2),
        ALUOpType.xor   -> (src1 ^ src2),
        ALUOpType.or    -> (src1 | src2),
        ALUOpType.srl   -> (src1 >> shamt),
        ALUOpType.sra   -> (SignExt(src1, 2 * XLEN) >> shamt)(XLEN - 1, 0),
        ALUOpType.sll   -> (src1 << shamt)(XLEN - 1, 0),
    ))

    io.res  := res
}
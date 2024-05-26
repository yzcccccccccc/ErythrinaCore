package erythcore.fu.mul

/*
    Top Wrapper for debug
    Author: yzcc
*/

import chisel3._
import chisel3.util._
import erythcore.ErythrinaDefault

class MulDebug extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val v   = Input(Bool())
        val a   = Input(UInt(XLEN.W))
        val b   = Input(UInt(XLEN.W))
        val op  = Input(UInt(2.W))              // 00: mul, 01: mulh, 10: mulhsu, 11: mulhu
        val res = Output(UInt(XLEN.W))
    })

    val (a, b)  = (io.a, io.b)

    val mul_inst    = Module(new Multiplier(len=(XLEN + 1)))

    val a_signed    = Cat(a(XLEN - 1), a)
    val a_unsigned  = Cat(0.U(1.W), a)

    val b_signed    = Cat(b(XLEN - 1), b)
    val b_unsigned  = Cat(0.U(1.W), b)

    val a_src   = Mux(io.op === "b11".U, a_unsigned, a_signed)
    val b_src   = Mux(io.op(1), b_unsigned, b_signed)

    val use_h   = io.op =/= "b00".U

    mul_inst.io.a   := a_src
    mul_inst.io.b   := b_src
    mul_inst.io.regEnables(0)   := io.v
    mul_inst.io.regEnables(1)   := RegNext(io.v)

    io.res  := Mux(use_h, mul_inst.io.res(2 * XLEN - 1, XLEN), mul_inst.io.res(XLEN - 1, 0))
}
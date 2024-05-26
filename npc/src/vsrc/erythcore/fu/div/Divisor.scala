package erythcore.fu.div

import chisel3._
import chisel3.util._
import erythcore.ErythrinaDefault

class DivDebug extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val v   = Input(Bool())
        val a   = Input(UInt(XLEN.W))
        val b   = Input(UInt(XLEN.W))
        val op  = Input(UInt(2.W))              // 00: div, 01: divu, 10: rem, 11: remu
        val res = Output(UInt(XLEN.W))
        val res_valid = Output(Bool())
    })

    val (a, b) = (io.a, io.b)
    
    val div_inst = Module(new DivCore(len=(XLEN + 1)))

    val a_signed    = Cat(a(XLEN - 1), a)
    val a_unsigned  = Cat(0.U(1.W), a)
    val b_signed    = Cat(b(XLEN - 1), b)
    val b_unsigned  = Cat(0.U(1.W), b)

    val a_src   = Mux(io.op(0), a_unsigned, a_signed)
    val b_src   = Mux(io.op(0), b_unsigned, b_signed)
    val use_rem = io.op(1)

    div_inst.io.in_v := io.v
    div_inst.io.flush := 0.B
    div_inst.io.a := a_src
    div_inst.io.b := b_src

    io.res := Mux(use_rem, div_inst.io.rem, div_inst.io.quot)
    io.res_valid := div_inst.io.out_v
}
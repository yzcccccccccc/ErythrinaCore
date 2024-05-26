package erythcore.fu.div

import chisel3._
import chisel3.util._

class DivCore(len : Int) extends Module{
    val io = IO(new Bundle {
        val in_v = Input(Bool())
        val flush = Input(Bool())
        val a = Input(UInt(len.W))
        val b = Input(UInt(len.W))

        val quot = Output(UInt(len.W))
        val rem = Output(UInt(len.W))
        val out_v = Output(Bool())
    })

    // FSM
    val compute_cnt = RegInit(0.U(6.W))
    val sIDLE :: sCOMPUTE :: sDONE :: Nil = Enum(3)
    val state = RegInit(sIDLE)
    switch (state){
        is (sIDLE){
            when (io.in_v & !io.flush){
                state := sCOMPUTE
            }
        }
        is (sCOMPUTE){
            when (compute_cnt === 1.U & !io.flush){
                state := sDONE
            }.elsewhen(io.flush){
                state := sIDLE
            }
        }
        is (sDONE){
            state := sIDLE
        }
    }

    when (state === sIDLE && io.in_v && !io.flush){
        compute_cnt := (len - 1).U
    }.elsewhen (state === sCOMPUTE){
        compute_cnt := compute_cnt - 1.U
    }

    // Div, caculate a/b
    val a_sign = io.a(len - 1)
    val b_sign = io.b(len - 1)
    val a = Mux(a_sign, (~io.a + 1.U).asUInt, io.a)
    val b = Mux(b_sign, (~io.b + 1.U).asUInt, io.b)
    val neg_b = (~b + 1.U).asUInt

    val quot_r  = Reg(UInt(len.W))
    val rem_r   = Reg(UInt(len.W))
    
    // Quotient
    val next_quot = Cat(quot_r(len - 2, 0), ~rem_r(len - 1))
    when (state === sIDLE && io.in_v && !io.flush){
        quot_r := 0.U
    }
    when (state === sCOMPUTE){
        quot_r := Cat(next_quot(len - 2, 0), 0.U(1.W))      // shift left
    }

    // Remainder
    val next_rem = Cat(rem_r(len - 2, 0), 0.U(1.W)) + Mux(rem_r(len - 1), b, neg_b)
    when (state === sIDLE && io.in_v && !io.flush){
        rem_r   := a + neg_b
    }
    when (state === sCOMPUTE){
        rem_r := next_rem
    }

    // Res
    val tmp_quot    = Cat(quot_r(len - 2, 0), ~rem_r(len - 1))
    io.quot := Mux(a_sign ^ b_sign, ~tmp_quot + 1.U, tmp_quot)
    io.rem  := rem_r
    io.out_v := state === sDONE | io.flush
}
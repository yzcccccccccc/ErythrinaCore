package erythcore.backend

/*
    * Dispatch & Issue Unit
    * Has 2 queues: Int Queue, Memory Queue
    * Eache queue has 8 items
*/

import chisel3._
import chisel3.util._
import erythcore._

class ISU extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val in = Decoupled(Input(new InstCtrlBlk))

        val out = Vec(2, Decoupled(Output(new InstCtrlBlk)))

        val bypass  = Input(Vec(2, Valid(new BypassBundle)))
    })

    val intRS = Module(new RS)
    val memRS = Module(new RS)

    val instblk = io.in.bits

    val use_int = instblk.basicInfo.instValid & instblk.basicInfo.fuType =/= FuType.lsu
    val use_mem = instblk.basicInfo.instValid & instblk.basicInfo.fuType === FuType.lsu

    intRS.io.enq.valid  := use_int
    intRS.io.enq.bits   := instblk
    intRS.io.bypass     := io.bypass
    intRS.io.deq    <> io.out(0)

    memRS.io.enq.valid  := use_mem
    memRS.io.enq.bits   := instblk
    memRS.io.bypass     := io.bypass
    memRS.io.deq    <> io.out(1)
}
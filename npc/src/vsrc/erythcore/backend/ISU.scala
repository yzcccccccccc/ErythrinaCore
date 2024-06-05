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
    })
}
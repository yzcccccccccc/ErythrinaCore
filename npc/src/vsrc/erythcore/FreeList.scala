package erythcore

import chisel3._
import chisel3.util._
import erythcore._

/*
    * FreeList: Physical Register Free List
    * Read Ports    : 1 (for 1 inst renaming)
    * Write Ports   : 2 (for 2 inst retiring)
*/

class DeqBundle extends Bundle with HasErythDefault{
    val free_prf = Output(UInt(PRFbits.W))
}

class EnqBundle extends Bundle with HasErythDefault{
    val free_prf = Input(Vec(2, UInt(PRFbits.W)))
    val wen      = Input(Vec(2, Bool()))
}

class FreeList extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val deq = Decoupled(new DeqBundle)
        val enq = Decoupled(new EnqBundle)
    })

    // Available Table (Physical Register)
    val avail_tab   = RegInit(VecInit(Seq.fill(NR_PRF)(1.B)))
    when (reset.asBool){
        for (i <- 0 until NR_ARF){
            avail_tab(i) := 0.B
        }
    }

    // Dequeue
    val free_list   = avail_tab.asUInt
    io.deq.bits.free_prf    := PriorityEncoder(free_list)
    io.deq.valid            := free_list.orR

    // Enqueue
    io.enq.ready := true.B
    for (i <- 0 until 2){
        when (io.enq.valid && io.enq.bits.wen(i)){
            avail_tab(io.enq.bits.free_prf(i)) := 1.B
        }
    }
}
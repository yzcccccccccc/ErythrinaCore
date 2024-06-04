package erythcore

import chisel3._
import chisel3.util._

trait HasFreeListDefault extends HasErythDefault{
    def DATA_WIDTH = log2Ceil(NR_PRF)
}

class DeqIO extends Bundle with HasFreeListDefault{
    val ren         = Input(Vec(2, Bool()))
    val free_prf    = Output(Vec(2, UInt(DATA_WIDTH.W)))
}

class EnqIO extends Bundle with HasFreeListDefault{
    val wen         = Input(Vec(2, Bool()))
    val alloc_prf   = Input(Vec(2, UInt(DATA_WIDTH.W)))
}

class FreeList extends Module with HasFreeListDefault{
    val io = IO(new Bundle{
        val deq = Decoupled(new DeqIO)
        val enq = Decoupled(new EnqIO)
    })

    val alloc_table = RegInit(VecInit(Seq.fill(NR_PRF)(true.B)))
    when (reset.asBool){
        for (i <- 0 until NR_ARF){
            alloc_table(i) := false.B
        }
    }

    // Deque
    val free_list_0 = alloc_table.asUInt
    val free_list_1 = free_list_0 - lowbit(free_list_0)

    io.deq.bits.free_prf(0) := Mux(free_list_0 === 0.U, 65.U, PriorityEncoder(free_list_0))
    io.deq.bits.free_prf(1) := Mux(free_list_1 === 0.U, 65.U, PriorityEncoder(free_list_1))

    val deq_valid_vec = Wire(Vec(2,Bool()))
    for (i <- 0 until 2){
        deq_valid_vec(i)    := io.deq.bits.ren(i) & io.deq.bits.free_prf(i) =/= 0.U
    }
    io.deq.valid    := deq_valid_vec.reduce(_ && _)

    // Enque
    io.enq.ready    := 1.B
    for (i <- 0 until 2){
        alloc_table(io.enq.bits.alloc_prf(i))   := true.B
    }
}

object lowbit {
    def apply(data: UInt): UInt = {
        data & (~data+1.U)
    }
}
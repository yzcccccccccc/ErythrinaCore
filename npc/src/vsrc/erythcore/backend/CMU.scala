package erythcore.backend

/*
    * Process Commit Info
*/

import chisel3._
import chisel3.util._
import erythcore._
import erythcore.common._

class CMU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        // From ROB
        val commit = Flipped(Decoupled(new ROBDeqBundle))

        // To FreeList
        val fl_enq = Flipped(Decoupled(new FLEnqBundle))
    })

    io.fl_enq.valid := io.commit.valid
    io.fl_enq.bits.free_prf(0) := io.commit.bits.entry_vec(0).p_rd
    io.fl_enq.bits.free_prf(1) := io.commit.bits.entry_vec(1).p_rd
    io.fl_enq.bits.wen(0) := io.commit.bits.valid_vec(0)
    io.fl_enq.bits.wen(1) := io.commit.bits.valid_vec(1)

    io.commit.ready := io.fl_enq.ready
}
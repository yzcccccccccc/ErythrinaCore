package erythcore.common

import chisel3._
import chisel3.util._
import erythcore._
import utils._

class ROBEntry extends Bundle with HasErythDefault{
    val exceptionVec    = Vec(ExceptionSetting.WIDTH, Bool())
    val instType        = UInt(4.W)
    val a_rd            = UInt(ARFbits.W)
    val p_rd            = UInt(PRFbits.W)
    val pp_rd           = UInt(PRFbits.W)       // previous physical destination of a_rd, for rolling back
    val pc              = UInt(XLEN.W)
    val rf_wen          = Bool()
    val isDone          = Bool()
}

// allocate a ROB entry
class ROBEnqBundle extends Bundle with HasErythDefault{
    val entry   = Input(new ROBEntry)

    val rob_idx = Output(UInt(ROBbits.W))
}

class ROBQueryBundle extends Bundle with HasErythDefault{
    val psrc1   = Input(UInt(PRFbits.W))
    val psrc2   = Input(UInt(PRFbits.W))
    val rdy1    = Output(Bool())
    val rdy2    = Output(Bool())
    val pause_idx1  = Output(UInt(ROBbits.W))
    val pause_idx2  = Output(UInt(ROBbits.W))
}

class ROBDeqBundle extends Bundle with HasErythDefault{
    val entry_vec   = Output(Vec(2, new ROBEntry))
    val rob_idx_vec = Output(Vec(2, UInt(ROBbits.W)))
    val valid_vec   = Output(Vec(2, Bool()))
}

class ROB extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val enq = Decoupled(new ROBEnqBundle)
        val deq = new ROBDeqBundle
        val query = new ROBQueryBundle
    })

    val rob = Mem(NR_ROB, new ROBEntry)

    val rob_head    = RegInit(0.U(ROBbits.W))
    val rob_tail    = RegInit(0.U(ROBbits.W))
    val rob_count   = RegInit(0.U((ROBbits + 1).W))

    // Enqueue
    io.enq.ready := rob_count < NR_ROB.U
    io.enq.bits.rob_idx := rob_tail
    when (io.enq.valid && io.enq.ready){
        rob(io.enq.bits.rob_idx) := io.enq.bits.entry
    }

    // Query
    def in_range(idx: UInt): Bool = {
        val res = Mux(rob_head <= rob_tail, rob_head <= idx && idx < rob_tail, idx < rob_tail || rob_head <= idx)
        res
    }

    val psrc1_hit_vec_o = Wire(Vec(NR_ROB, Bool()))     // origin
    val psrc2_hit_vec_o = Wire(Vec(NR_ROB, Bool()))
    for (i <- 0 until NR_ROB){
        psrc1_hit_vec_o(i) := rob(i).p_rd === io.query.psrc1 & in_range(i.U) & ~rob(i).isDone & rob(i).rf_wen
        psrc2_hit_vec_o(i) := rob(i).p_rd === io.query.psrc2 & in_range(i.U) & ~rob(i).isDone & rob(i).rf_wen
    }
    val psrc1_hit_vec   = GenLshiftedVec(psrc1_hit_vec_o, rob_head)
    val psrc2_hit_vec   = GenLshiftedVec(psrc2_hit_vec_o, rob_head)

    io.query.rdy1   := psrc1_hit_vec.contains(1.B)
    io.query.rdy2   := psrc1_hit_vec.contains(1.B)
    io.query.pause_idx1 := (psrc1_hit_vec.lastIndexWhere(_ === 1.B) + rob_head).asUInt
    io.query.pause_idx2 := (psrc2_hit_vec.lastIndexWhere(_ === 1.B) + rob_head).asUInt

    // Commit(Deq) 2 Entries?
    io.deq.valid_vec(0) := rob_count >= 1.U & rob(rob_head).isDone
    io.deq.entry_vec(0) := rob(rob_head)
    io.deq.rob_idx_vec(0)   := rob_head
    
    io.deq.valid_vec(1) := rob_count >= 2.U & rob(rob_head + 1.U).isDone & rob(rob_head).isDone
    io.deq.entry_vec(1) := rob(rob_head + 1.U)
    io.deq.rob_idx_vec(1)   := rob_head + 1.U

    // ptr update
    val deq_count   = io.deq.valid_vec.count(_ === 1.B)
    rob_head    := rob_head + deq_count
    when (io.enq.fire){
        rob_tail    := rob_tail + 1.U
        rob_count   := rob_count + 1.U - deq_count
    }.otherwise{
        rob_count   := rob_count - deq_count
    }

    // TODO: add branch and exception handler
}
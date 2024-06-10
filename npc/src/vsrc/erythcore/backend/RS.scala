package erythcore.backend

import chisel3._
import chisel3.util._
import erythcore._

/*
    * Reservation Stack (Issue Queue)
*/

trait HasRSTrait extends HasErythDefault{
    val RS_DEPTH    = 8
    val RSbits      = log2Ceil(RS_DEPTH)
}

class RS extends Module with HasRSTrait{
    val io = IO(new Bundle{
        val enq = Decoupled(Input(new InstCtrlBlk))
        val deq = Flipped(Decoupled(Output(new InstCtrlBlk)))
        val bypass  = Input(Vec(2, Valid(new BypassBundle)))
    })

    // Stack Status
    val rs      = Mem(RS_DEPTH, new InstCtrlBlk)
    val rs_rdy  = RegInit(VecInit(Seq.fill(RS_DEPTH)(false.B)))
    val sc      = RegInit(0.U((RSbits + 1).W))     // stack counter

    // Bypass
    def gen_new_entry(old_entry:InstCtrlBlk):InstCtrlBlk = {
        val new_entry   = old_entry

        val hit1 = io.bypass(0).bits.rob_idx === new_entry.pause_rob_idx1 & ~new_entry.rdy1 & io.bypass(0).valid
        val hit2 = io.bypass(0).bits.rob_idx === new_entry.pause_rob_idx2 & ~new_entry.rdy2 & io.bypass(0).valid
        new_entry.src1_dat  := Mux(hit1, io.bypass(0).bits.res, new_entry.src1_dat)
        new_entry.src2_dat  := Mux(hit2, io.bypass(0).bits.res, new_entry.src2_dat)
        new_entry.rdy1      := Mux(hit1, true.B, new_entry.rdy1)
        new_entry.rdy2      := Mux(hit2, true.B, new_entry.rdy2)
        new_entry
    }
    
    def chk_rdy(entry:InstCtrlBlk):Bool = {
        entry.rdy1 & entry.rdy2
    }

    // Issue Select (?)
    val iss_idx     = PriorityEncoder(rs_rdy.asUInt)
    val iss_valid   = rs_rdy.asUInt =/= 0.U

    // Update
    when (io.deq.fire & ~io.enq.fire){      // only dequeue
        var new_entry   = Wire(new InstCtrlBlk)
        for (i <- 0 until RS_DEPTH - 1){
            when (i.U >= iss_idx){
                new_entry   := gen_new_entry(rs(i + 1))
                rs(i)       := new_entry
                rs_rdy(i)   := chk_rdy(new_entry)
            }
        }
        rs_rdy(RS_DEPTH - 1) := false.B
        sc := sc - 1.U
    }.elsewhen(~io.deq.fire & io.enq.fire){ // only enqueue
        var new_entry   = Wire(new InstCtrlBlk)
        new_entry   := gen_new_entry(io.enq.bits)
        rs(sc)      := new_entry
        rs_rdy(sc)  := chk_rdy(new_entry)
        sc := sc + 1.U
    }.elsewhen(io.deq.fire & io.enq.fire){  // both
        var new_entry   = Wire(new InstCtrlBlk)
        for (i <- 0 until RS_DEPTH - 1){
            when (i.U >= iss_idx){
                new_entry   := gen_new_entry(rs(i + 1))
                rs(i)       := new_entry
                rs_rdy(i)   := chk_rdy(new_entry)
            }
        }
        rs_rdy(RS_DEPTH - 1) := false.B

        new_entry   := gen_new_entry(io.enq.bits)
        rs(sc)      := new_entry
        rs_rdy(sc)  := chk_rdy(new_entry)
    }

    // Enqueue
    io.enq.ready := sc < RS_DEPTH.U

    // Dequeue
    io.deq.valid := iss_valid
    io.deq.bits  := rs(iss_idx)
}
package erythcore.backend

import chisel3._
import chisel3.util._
import erythcore._

class ROBEntry extends Bundle with HasErythDefault{
    val exceptionVec    = Vec(ExceptionSetting.WIDTH, Bool())
    val instType        = UInt(4.W)
    val a_rd            = UInt(ARFbits.W)
    val p_rd            = UInt(PRFbits.W)
    val pp_rd           = UInt(PRFbits.W)       // previous physical destination of a_rd, for rolling back
    val pc              = UInt(XLEN.W)
    val isDone          = Bool()
}

// allocate a ROB entry
class ROBEnq extends Bundle with HasErythDefault{
    val entry   = Input(new ROBEntry)

    val psrc1   = Input(UInt(PRFbits.W))
    val psrc2   = Input(UInt(PRFbits.W))
    val rdy1    = Output(Bool())
    val rdy2    = Output(Bool())

    val rob_idx = Output(UInt(ROBbits.W))
}

class ROB extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val enq = Decoupled(new ROBEnq)
    })

    val rob = Mem(NR_ROB, new ROBEntry)

    val rob_head    = RegInit(0.U(ROBbits.W))
    val rob_tail    = RegInit(0.U(ROBbits.W))
    val rob_count   = RegInit(0.U(ROBbits.W))

    // Enqueue
    io.enq.ready := rob_count < NR_ROB.U
    io.enq.bits.rob_idx := rob_tail
    when (io.enq.valid && io.enq.ready){
        rob(io.enq.bits.rob_idx) := io.enq.bits.entry
        rob_count := rob_count + 1.U
    }
}
package erythcore

import chisel3._
import chisel3.util._

trait ErythrinaDefault {
    // General Global Settings
    val XLEN = 32           // RV32
    val RESETVEC    = 0x80000000L
    val MASKLEN = XLEN/8         // 32/8=4
    val RESPLEN = 2

    val arch = "multi"
}

class ErythrinaCommit extends Bundle with ErythrinaDefault with RegTrait{
    val pc          = Output(UInt(XLEN.W))
    val inst        = Output(UInt(XLEN.W))
    val rf_wen      = Output(Bool())
    val rf_waddr    = Output(UInt(RegAddrLen.W))
    val rf_wdata    = Output(UInt(XLEN.W))
    val valid       = Output(Bool())
}
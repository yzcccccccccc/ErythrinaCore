package erythcore

import chisel3._
import chisel3.util._

object ErythrinaSetting{
    var RESETVEC    = 0x30000000L
    var isSTA       = false
}

trait ErythrinaDefault {
    // General Global Settings
    val XLEN = 32           // RV32
    val MASKLEN = XLEN/8         // 32/8=4
    val RESPLEN = 2

    val LATENCY = 0
    val MARCH = "P"
}

class ErythrinaCommit extends Bundle with ErythrinaDefault with RegTrait{
    val pc          = Output(UInt(XLEN.W))
    val inst        = Output(UInt(XLEN.W))
    val rf_wen      = Output(Bool())
    val rf_waddr    = Output(UInt(RegAddrLen.W))
    val rf_wdata    = Output(UInt(XLEN.W))
    val mem_addr    = Output(UInt(XLEN.W))
    val mem_en      = Output(Bool())
    val valid       = Output(Bool())
}
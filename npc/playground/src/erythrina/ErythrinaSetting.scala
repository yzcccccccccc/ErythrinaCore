package ErythrinaCore

import chisel3._
import chisel3.util._

trait ErythrinaDefault {
    // General Global Settings
    val XLEN = 32           // RV32
    val RESETVEC    = 0x80000000L
    val RegNum          = 32
    val RegAddrLen      = 5         // log(32)
}
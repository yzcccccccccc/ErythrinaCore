package erythcore

import chisel3._
import chisel3.util._

trait HasErythDefault{
    val XLEN = 32
    val MASKLEN = XLEN / 8
    val RESPLEN = 2

    val LATENCY = 0

    val NR_ARF  = 32
    val NR_PRF  = 64
    val ARFbits = log2Ceil(NR_ARF)
    val PRFbits = log2Ceil(NR_PRF)

    val NR_ROB  = 32
    val ROBbits = log2Ceil(NR_ROB)
}

object ErythSetting{
    var RESETVEC    = 0x30000000L
    var isSTA       = false
}
package ErythrinaCore

import chisel3._
import chisel3.util._

import utils._

object  RV32CSR extends InstrType{
    def EBREAK   = BitPat("b0000000_00001_00000_000_00000_11100_11")

    val table = Array(
        EBREAK      -> List(TypeI, ALUop.nop, LSUop.nop, BPUop.nop, CSRop.ebreak)
    )
}
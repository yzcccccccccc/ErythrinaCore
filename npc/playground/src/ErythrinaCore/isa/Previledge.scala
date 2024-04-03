package ErythrinaCore

import chisel3._
import chisel3.util._

import utils._

object Previledge extends  InstrType{
    def EBREAK  = BitPat("b0000000_00001_00000_000_00000_11100_11")
    def ECALL   = BitPat("b0000000_00000_00000_000_00000_11100_11")

    // Mache Level
    def MRET    = BitPat("b0011000_00010_00000_000_00000_11100_11")

    val table = Array(
        EBREAK      -> List(TypeI, ALUop.nop, LSUop.nop, BPUop.nop, CSRop.jmp),
        ECALL       -> List(TypeI, ALUop.nop, LSUop.nop, BPUop.nop, CSRop.jmp),
        MRET        -> List(TypeI, ALUop.nop, LSUop.nop, BPUop.nop, CSRop.jmp)
    )
}
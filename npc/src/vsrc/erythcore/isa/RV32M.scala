package erythcore

import chisel3._
import chisel3.util._

object RV32M extends InstrType{
    def MUL     = BitPat("b0000001_?????_?????_000_?????_01100_11")
    def MULH    = BitPat("b0000001_?????_?????_001_?????_01100_11")
    def MULHSU  = BitPat("b0000001_?????_?????_010_?????_01100_11")
    def MULHU   = BitPat("b0000001_?????_?????_011_?????_01100_11")

    def DIV     = BitPat("b0000001_?????_?????_100_?????_01100_11")
    def DIVU    = BitPat("b0000001_?????_?????_101_?????_01100_11")
    def REM     = BitPat("b0000001_?????_?????_110_?????_01100_11")
    def REMU    = BitPat("b0000001_?????_?????_111_?????_01100_11")

    val table = Array(
        MUL     -> List(TypeR, ALUop.mul, LSUop.nop, BPUop.nop, CSRop.nop),
        MULH    -> List(TypeR, ALUop.mulh, LSUop.nop, BPUop.nop, CSRop.nop),
        MULHSU  -> List(TypeR, ALUop.mulhsu, LSUop.nop, BPUop.nop, CSRop.nop),
        MULHU   -> List(TypeR, ALUop.mulhu, LSUop.nop, BPUop.nop, CSRop.nop),
        DIV     -> List(TypeR, ALUop.div, LSUop.nop, BPUop.nop, CSRop.nop),
        DIVU    -> List(TypeR, ALUop.divu, LSUop.nop, BPUop.nop, CSRop.nop),
        REM     -> List(TypeR, ALUop.rem, LSUop.nop, BPUop.nop, CSRop.nop),
        REMU    -> List(TypeR, ALUop.remu, LSUop.nop, BPUop.nop, CSRop.nop)
    )
}
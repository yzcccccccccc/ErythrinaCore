package erythcore.isa

/*
    * RISC-V I extention
    * currently only support RV32
*/

import chisel3._
import chisel3.util._
import erythcore._
import erythcore.backend.fu._

object RV32I_alu extends HasInstType{
    def ADDI    = BitPat("b???????_?????_?????_000_?????_00100_11")
    def SLTI    = BitPat("b???????_?????_?????_010_?????_00100_11")
    def SLTIU   = BitPat("b???????_?????_?????_011_?????_00100_11")
    def XORI    = BitPat("b???????_?????_?????_100_?????_00100_11")
    def ORI     = BitPat("b???????_?????_?????_110_?????_00100_11")
    def ANDI    = BitPat("b???????_?????_?????_111_?????_00100_11")
    def SLLI    = BitPat("b0000000_?????_?????_001_?????_00100_11")
    def SRLI    = BitPat("b0000000_?????_?????_101_?????_00100_11")
    def SRAI    = BitPat("b0100000_?????_?????_101_?????_00100_11")

    def ADD     = BitPat("b0000000_?????_?????_000_?????_01100_11")
    def SUB     = BitPat("b0100000_?????_?????_000_?????_01100_11")
    def SLL     = BitPat("b0000000_?????_?????_001_?????_01100_11")
    def SLT     = BitPat("b0000000_?????_?????_010_?????_01100_11")
    def SLTU    = BitPat("b0000000_?????_?????_011_?????_01100_11")
    def XOR     = BitPat("b0000000_?????_?????_100_?????_01100_11")
    def SRL     = BitPat("b0000000_?????_?????_101_?????_01100_11")
    def SRA     = BitPat("b0100000_?????_?????_101_?????_01100_11")
    def OR      = BitPat("b0000000_?????_?????_110_?????_01100_11")
    def AND     = BitPat("b0000000_?????_?????_111_?????_01100_11")

    def AUIPC   = BitPat("b???????_?????_?????_???_?????_00101_11")
    def LUI     = BitPat("b???????_?????_?????_???_?????_01101_11")

    val table = Array(
        ADDI    -> List(InstI, FuType.alu, ALUOpType.add),
        SLTI    -> List(InstI, FuType.alu, ALUOpType.slt),
        SLTIU   -> List(InstI, FuType.alu, ALUOpType.sltu),
        XORI    -> List(InstI, FuType.alu, ALUOpType.xor),
        ORI     -> List(InstI, FuType.alu, ALUOpType.or),
        ANDI    -> List(InstI, FuType.alu, ALUOpType.and),
        SLLI    -> List(InstI, FuType.alu, ALUOpType.sll),
        SRLI    -> List(InstI, FuType.alu, ALUOpType.srl),
        SRAI    -> List(InstI, FuType.alu, ALUOpType.sra),

        ADD     -> List(InstR, FuType.alu, ALUOpType.add),
        SUB     -> List(InstR, FuType.alu, ALUOpType.sub),
        SLL     -> List(InstR, FuType.alu, ALUOpType.sll),
        SLT     -> List(InstR, FuType.alu, ALUOpType.slt),
        SLTU    -> List(InstR, FuType.alu, ALUOpType.sltu),
        XOR     -> List(InstR, FuType.alu, ALUOpType.xor),
        SRL     -> List(InstR, FuType.alu, ALUOpType.srl),
        SRA     -> List(InstR, FuType.alu, ALUOpType.sra),
        OR      -> List(InstR, FuType.alu, ALUOpType.or),
        AND     -> List(InstR, FuType.alu, ALUOpType.and),

        AUIPC   -> List(InstU, FuType.alu, ALUOpType.add),
        LUI     -> List(InstU, FuType.alu, ALUOpType.nop)
    )
}

object RV32I_bru extends HasInstType{
    def BEQ     = BitPat("b???????_?????_?????_000_?????_11000_11")
    def BNE     = BitPat("b???????_?????_?????_001_?????_11000_11")
    def BLT     = BitPat("b???????_?????_?????_100_?????_11000_11")
    def BGE     = BitPat("b???????_?????_?????_101_?????_11000_11")
    def BLTU    = BitPat("b???????_?????_?????_110_?????_11000_11")
    def BGEU    = BitPat("b???????_?????_?????_111_?????_11000_11")

    def JALR    = BitPat("b???????_?????_?????_000_?????_11001_11")
    def JAL     = BitPat("b???????_?????_?????_???_?????_11011_11")

    val table = Array(
        BEQ     -> List(InstB, FuType.bru, BRUOpType.beq),
        BNE     -> List(InstB, FuType.bru, BRUOpType.bne),
        BLT     -> List(InstB, FuType.bru, BRUOpType.blt),
        BGE     -> List(InstB, FuType.bru, BRUOpType.bge),
        BLTU    -> List(InstB, FuType.bru, BRUOpType.bltu),
        BGEU    -> List(InstB, FuType.bru, BRUOpType.bgeu),

        JALR    -> List(InstI, FuType.bru, BRUOpType.jalr),
        JAL     -> List(InstJ, FuType.bru, BRUOpType.jal)
    )
}

object RV32I_lsu extends HasInstType{
    def LB      = BitPat("b???????_?????_?????_000_?????_00000_11")
    def LH      = BitPat("b???????_?????_?????_001_?????_00000_11")
    def LW      = BitPat("b???????_?????_?????_010_?????_00000_11")
    def LBU     = BitPat("b???????_?????_?????_100_?????_00000_11")
    def LHU     = BitPat("b???????_?????_?????_101_?????_00000_11")
    def SB      = BitPat("b???????_?????_?????_000_?????_01000_11")
    def SH      = BitPat("b???????_?????_?????_001_?????_01000_11")
    def SW      = BitPat("b???????_?????_?????_010_?????_01000_11")

    val table = Array(
        LB      -> List(InstI, FuType.lsu, LSUOpType.lb),
        LH      -> List(InstI, FuType.lsu, LSUOpType.lh),
        LW      -> List(InstI, FuType.lsu, LSUOpType.lw),
        LBU     -> List(InstI, FuType.lsu, LSUOpType.lbu),
        LHU     -> List(InstI, FuType.lsu, LSUOpType.lhu),
        SB      -> List(InstS, FuType.lsu, LSUOpType.sb),
        SH      -> List(InstS, FuType.lsu, LSUOpType.sh),
        SW      -> List(InstS, FuType.lsu, LSUOpType.sw)
    )
}

object RVI{
    val table = RV32I_alu.table ++ RV32I_bru.table ++ RV32I_lsu.table
}
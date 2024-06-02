package erythcore

import chisel3._
import chisel3.util._
import erythcore.backend.fu._
import erythcore.isa._

trait HasInstType{
    def InstN   = "b0000".U
    def InstI   = "b0100".U
    def InstR   = "b0101".U
    def InstS   = "b0010".U
    def InstB   = "b0001".U
    def InstU   = "b0110".U
    def InstJ   = "b0111".U

    def need_rf_wen(instType:UInt): Bool = instType(2)
}

object SrcType{
    // TODO
    def imm     = "b00".U
    def reg     = "b01".U
    def pc      = "b10".U
    def const   = "b11".U

    def apply() = UInt(3.W)
}

object FuType {
    def alu = "b000".U      // ALU
    def bru = "b001".U      // BRU (Branch)
    def mdu = "b010".U      // MDU (Mul & Div)
    def lsu = "b100".U      // LSU (Load & Store)
    def csr = "b110".U      // CSR
    
    def apply() = UInt(2.W)
}

object FuOpType{
    def apply() = UInt(7.W)
}

object Instructions extends HasInstType with HasErythDefault{
    def nop = 0x00000013.U
    val DecodeDefault   = List(InstN, FuType.alu, ALUOpType.nop)
    val DecodeTable     = RVI.table
}
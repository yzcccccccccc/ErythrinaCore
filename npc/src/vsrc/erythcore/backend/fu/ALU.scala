package erythcore.backend.fu

import chisel3._
import chisel3.util._

object ALUOpType{
    def nop  = "b00000".U
    def add  = "b00001".U
    def sub  = "b10000".U
    def slt  = "b10001".U
    def sltu = "b10010".U
    def and  = "b00100".U
    def or   = "b00101".U
    def xor  = "b00110".U
    def sll  = "b00111".U
    def srl  = "b01000".U
    def sra  = "b01001".U

    def usesub(aluop:UInt):Bool = aluop(4)
}
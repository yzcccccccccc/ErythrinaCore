package erythcore.backend.fu

import chisel3._
import chisel3.util._
import erythcore._

object BRUOpType{
    def nop  = "b0000".U
    def beq  = "b0001".U
    def bne  = "b0010".U
    def blt  = "b0011".U
    def bge  = "b0100".U
    def bltu = "b0101".U
    def bgeu = "b0110".U
    def jal  = "b1000".U
    def jalr = "b1001".U
}

class BRU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val src1    = Input(UInt(XLEN.W))
        val src2    = Input(UInt(XLEN.W))
        val bruop   = Input(FuOpType())

        val res     = Output(Bool())
    })
}
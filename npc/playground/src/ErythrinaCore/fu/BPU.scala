package ErythrinaCore

import chisel3._
import chisel3.util._

trait BPUtrait extends ErythrinaDefault{
    val BPUopLEN = 4
}

object BPUop{
    def nop     = "b0000".U
    def jump    = "b0001".U         // jal or jalr
    def beq     = "b0010".U
    def bne     = "b0011".U
    def blt     = "b0100".U
    def bge     = "b0101".U
    def bltu    = "b0110".U
    def bgeu    = "b0111".U
}

// TODO: plan to implement a 2-bits guesser !
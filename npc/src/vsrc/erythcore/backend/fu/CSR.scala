package erythcore.backend.fu

import chisel3._
import chisel3.util._

object CSROpType {
    def jmp     = "b000".U
    def wrt     = "b001".U         // write
    def set     = "b010".U         // set
    def clr     = "b011".U
    def wrti    = "b101".U
    def seti    = "b110".U
    def clri    = "b111".U

    def usei(csrop: UInt)   = csrop(2)
    def iswrt(csrop: UInt)  = ~csrop(1) & csrop(0)
    def isset(csrop: UInt)  = csrop(1) & ~csrop(0)
    def isclr(csrop: UInt)  = csrop(1) & csrop(1)
}
package ErythrinaCore

import chisel3._
import chisel3.util._

object CSRop {
    def nop     = "b0000".U
    def ebreak  = "b0001".U
}
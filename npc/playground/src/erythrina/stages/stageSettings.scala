package ErythrinaCore

import chisel3._
import chisel3.util._

// IFU
trait IFUtrait extends ErythrinaDefault{
  // Exception Info (to be continued)
}

class IF2IDzip extends Bundle with IFUtrait{
  val inst  = UInt(XLEN.W)
  val pc    = UInt(XLEN.W)
}

// IDU
trait IDUtrait extends ErythrinaDefault with ALUtrait{

}

class ID2EXzip extends Bundle with IDUtrait{
    val src1    = UInt(XLEN.W)
    val src2    = UInt(XLEN.W)
    val aluop   = UInt(ALUopLEN.W)
}
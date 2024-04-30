package erythcore

import chisel3._
import chisel3.util._

// FU trait
trait FUtrait extends ErythrinaDefault with ALUtrait with LSUtrait with BPUtrait with RegTrait with CSRtrait{

}

// IFU
trait IFUtrait extends ErythrinaDefault with FUtrait{
  // Exception Info (to be continued)
}

class IF2IDzip extends Bundle with IFUtrait{
  val inst  = UInt(XLEN.W)
  val pc    = UInt(XLEN.W)
}

// IDU
trait IDUtrait extends ErythrinaDefault with FUtrait with InstrType{

}

class ID2EXzip extends Bundle with IDUtrait{
  val inst  = UInt(XLEN.W)
  val pc    = UInt(XLEN.W)

  // for ALU, BPU (EX), CSR
  val src1  = UInt(XLEN.W)
  val src2  = UInt(XLEN.W)
  val ALUop = UInt(ALUopLEN.W)
  val BPUop = UInt(BPUopLEN.W)
  val CSRop = UInt(CSRopLEN.W)

  // for LSU (MEM)
  val data2store  = UInt(XLEN.W)
  val LSUop       = UInt(LSUopLen.W)

  // for WBU (WB)
  val rd      = UInt(RegAddrLen.W)
  val rf_wen  = Bool() 
}

// EXU
trait EXUtrait extends ErythrinaDefault with FUtrait{

}

class EX2MEMzip extends Bundle with EXUtrait{
  val inst  = UInt(XLEN.W)
  val pc    = UInt(XLEN.W)

  // for LSU (MEM)
  val LSUop       = UInt(LSUopLen.W)
  val data2store  = UInt(XLEN.W)
  val addr        = UInt(XLEN.W)

  // for WBU (WB)
  val rd      = UInt(RegAddrLen.W)
  val rf_wen  = Bool()
}

// MEMU
trait MEMUtrait extends ErythrinaDefault with FUtrait{

}

class MEM2WBzip extends Bundle with MEMUtrait{
  val inst  = UInt(XLEN.W)
  val pc    = UInt(XLEN.W)

  // for WBU (WB)
  val RegWriteIO  = Flipped(new RegFileOUT)

  // mem commit
  val maddr = UInt(XLEN.W)
  val men   = Bool()
}

// WBU
trait WBUtrait extends ErythrinaDefault with FUtrait{
  
}

// Tool
object StageConnect extends ErythrinaDefault{
  def apply[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    if (ARCH == "single")
        right.bits := left.bits
    else
      if (ARCH == "multi")
          right <> left
      else
        if (ARCH == "pipeline")
          // TODO: to be continued
          right <> RegEnable(left, left.fire)
  }
}
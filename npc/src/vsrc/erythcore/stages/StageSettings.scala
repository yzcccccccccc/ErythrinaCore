package erythcore

import chisel3._
import chisel3.util._

class ExceptionVec extends Bundle with ErythrinaDefault{
  val isEbreak  = Bool()
  val isUnknown = Bool()
}

// FU trait
trait FUtrait extends ErythrinaDefault with ALUtrait with LSUtrait with BPUtrait with RegTrait with CSRtrait{}

// IFU
trait IFUtrait extends ErythrinaDefault with FUtrait{
  // Exception Info (to be continued)
}

class if_to_id_zip extends Bundle with IFUtrait{
  val content_valid = Bool()
  val inst = UInt(XLEN.W)
  val pc   = UInt(XLEN.W)
}

// IDU
trait IDUtrait extends ErythrinaDefault with FUtrait with InstrType{

}

class id_to_ex_zip extends Bundle with IDUtrait{
  val content_valid = Bool()
  val inst = UInt(XLEN.W)
  val pc   = UInt(XLEN.W)

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

  // exception
  val exception = new ExceptionVec
}

// EXU
trait EXUtrait extends ErythrinaDefault with FUtrait{

}

class ex_to_mem_zip extends Bundle with EXUtrait{
  val content_valid = Bool()
  val inst  = UInt(XLEN.W)
  val pc    = UInt(XLEN.W)

  // for LSU (MEM)
  val LSUop       = UInt(LSUopLen.W)
  val data2store  = UInt(XLEN.W)
  val addr        = UInt(XLEN.W)

  // for WBU (WB)
  val rd      = UInt(RegAddrLen.W)
  val rf_wen  = Bool()

  // exception
  val exception = new ExceptionVec
}

// MEMU
trait MEMUtrait extends ErythrinaDefault with FUtrait{

}

class mem_to_wb_zip extends Bundle with MEMUtrait{
  val content_valid = Bool()
  val inst  = UInt(XLEN.W)
  val pc    = UInt(XLEN.W)

  // for WBU (WB)
  val RegWriteIO  = Flipped(new RegFileOUT)

  // mem commit
  val maddr = UInt(XLEN.W)
  val men   = Bool()

  // exception
  val exception = new ExceptionVec
}

// WBU
trait WBUtrait extends ErythrinaDefault with FUtrait{
  
}

// Tool
object StageConnect extends ErythrinaDefault{
  def apply[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    right.valid := left.valid
    left.ready := right.ready
    right.bits := RegEnable(left.bits, right.fire)
  }
}
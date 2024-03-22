package ErythrinaCore

import chisel3._
import chisel3.util._

// LSU: for Load/Store Operations

trait LSUtrait extends ErythrinaDefault{
  val LSUopLen = 4
}

object LSUop{
  def nop     = "b1000".U     // don't use lsu
  def lb      = "b0000".U
  def lh      = "b0001".U
  def lw      = "b0010".U
  def lbu     = "b0011".U
  def lhu     = "b0100".U

  def sb      = "b0101".U
  def sh      = "b0110".U
  def sw      = "b0111".U
}

class LSUIO extends Bundle with LSUtrait{
  val addr    = Input(UInt(XLEN.W))
  val lsuop   = Input(UInt(LSUopLen.W))
  val data    = Output(UInt(XLEN.W))
}

class LSU extends Module with LSUtrait{
  val io = IO(new LSUIO)

  val addr  = io.addr
  val lsuop = io.lsuop

  // to be continued in the future
}
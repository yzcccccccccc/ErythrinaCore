package ErythrinaCore

import chisel3._
import chisel3.util._

// Instruction Type
trait InstrType{
  def TypeI = "b000".U
  def TypeR = "b001".U
  def TypeS = "b010".U
  def TypeU = "b011".U
  def TypeJ = "b100".U
  def TypeB = "b101".U
}
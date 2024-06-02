package erythcore.backend.fu

import chisel3._
import chisel3.util._

object LSUOpType{
  def lb      = "b000".U
  def lh      = "b001".U
  def lw      = "b010".U
  def lbu     = "b011".U
  def lhu     = "b100".U

  def sb      = "b101".U
  def sh      = "b110".U
  def sw      = "b111".U

  def isStore(op: UInt) = op(2) & op(1, 0).orR
  def isLoad(op: UInt)  = ~isStore(op)
}
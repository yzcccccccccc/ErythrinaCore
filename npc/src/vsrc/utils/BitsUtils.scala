package utils

import chisel3._
import chisel3.util._

object SignExt {
  def apply(a: UInt, len: Int) = {
    val aLen = a.getWidth
    val signBit = a(aLen-1)
    if (aLen >= len) a(len-1,0) else Cat(Fill(len - aLen, signBit), a)
  }
}

object ZeroExt {
  def apply(a: UInt, len: Int) = {
    val aLen = a.getWidth
    if (aLen >= len) a(len-1,0) else Cat(0.U((len - aLen).W), a)
  }
}

object MaskExpand{
  def apply(m: UInt) = {
    Cat(m.asBools.map(Fill(8, _)).reverse)
  }
}

object GenLshiftedVec{
  def apply[T <: Data](v: Vec[T], n: UInt) = {
    val res = Wire(Vec(v.size, v(0)))
    var idx = Wire(UInt((log2Ceil(v.length)).W))
    for (i <- 0 until v.length){
      idx := (i.U + idx).asUInt
      res(i) := v(idx)
    }
    res
  }
}
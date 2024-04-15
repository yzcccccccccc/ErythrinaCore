package utils

import chisel3._
import chisel3.util._
import chisel3.util.random.LFSR

class LatencyPipe[T <: Data](typ: T, latency: Int) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(DecoupledIO(typ))
    val out = DecoupledIO(typ)
  })

  val reg_valid = Reg(Vec(latency, Bool()))
  val reg_ready = Reg(Vec(latency, Bool()))
  val reg_bits  = Reg(Vec(latency, io.in.cloneType))

  // delaying
  reg_valid(0)  := io.in.valid
  reg_bits(0)   := io.in.bits
  for (i <- 0 until latency){
    reg_valid(i + 1)  := reg_valid(i)
    reg_bits(i + 1)   := reg_bits(i)
  }

  reg_ready(0)  := io.out.ready
  for (i <- 0 until latency){
    reg_ready(i + 1)  := reg_ready(i)
  }

  val delay = LFSR(latency)

  io.in.ready   := reg_ready(delay)
  io.out.valid  := reg_valid(delay)
  io.out.bits   := reg_bits(delay)
}

object LatencyPipe {
  def apply[T <: Data](in: DecoupledIO[T], out: DecoupledIO[T], latency: Int) = {
    val pipe = Module(new LatencyPipe(in.bits.cloneType, latency))
    pipe.io.in  <> in
    pipe.io.out <> out
  }
}

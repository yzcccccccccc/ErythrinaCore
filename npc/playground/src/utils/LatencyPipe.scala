package utils

import chisel3._
import chisel3.util._
import chisel3.util.random.LFSR
import chisel3.reflect.DataMirror

class LatencyPipe[T <: Data](typ: T, latency: Int) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(DecoupledIO(typ))
    val out = DecoupledIO(typ)
  })

  val reg_valid = Reg(Vec(latency, Bool()))
  val reg_ready = Reg(Vec(latency, Bool()))
  val reg_bits  = Reg(Vec(latency, DataMirror.internal.chiselTypeClone(io.in.bits)))

  // delaying
  reg_valid(0)  := io.in.valid
  reg_bits(0)   := io.in.bits
  for (i <- 0 until latency - 1){
    reg_valid(i + 1)  := reg_valid(i)
    reg_bits(i + 1)   := reg_bits(i)
  }

  reg_ready(0)  := io.out.ready
  for (i <- 0 until latency - 1){
    reg_ready(i + 1)  := reg_ready(i)
  }

  val delay = LFSR(log2Ceil(latency))

  io.in.ready   := reg_ready(delay)
  io.out.valid  := reg_valid(delay)
  io.out.bits   := reg_bits(delay)
}

object LatencyPipe {
  def apply[T <: Data](in: DecoupledIO[T], out: DecoupledIO[T], latency: Int) = {
    val pipe = Module(new LatencyPipe(DataMirror.internal.chiselTypeClone(in.bits), latency))
    pipe.io.in  <> in
    pipe.io.out <> out
  }
}

class LatencyPipeBit(latency: Int) extends Module{
  val io = IO(new Bundle {
    val in  = Input(Bool())
    val out = Output(Bool())
  })

  val cnt = Reg(UInt(4.W))
  val lim = Reg(UInt(4.W))

  if (latency > 0){
    // FSM
    val sIDLE :: sWORK :: sDONE :: Nil = Enum(3)
    val state = RegInit(sIDLE)

    switch (state){
      is (sIDLE){
        when (io.in){
          state := sWORK
        }
      }
      is (sWORK){
        when (cnt === lim){
          state := sDONE
        }
      }
      is (sDONE){
        when (~io.in){
          state := sIDLE
        }
      }
    }

    when (state === sIDLE & io.in){
      cnt := 0.U
      lim := LFSR(log2Ceil(latency))
    }    

    when (state === sWORK){
      cnt := cnt + 1.U
    }

    io.out  := state === sDONE
  }
  else{
    io.out  := io.in
  }
}

object LatencyPipeBit{
  def apply(in: chisel3.Bool, latency:Int) = {
    val pipe = Module(new LatencyPipeBit(latency))
    pipe.io.in  := in
    pipe.io.out
  }
}
package utils

import chisel3._
import chisel3.util._
import chisel3.util.random.LFSR
import erythcore.ErythrinaDefault

class LatencyPipeRis(latency:Int) extends Module{
    val io = IO(new Bundle {
        val in  = Input(Bool())
        val out = Output(Bool())
    })

    if (latency > 0){
        val cnt = Reg(UInt(4.W))
        val lim = Reg(UInt(4.W))

        val sIDLE :: sCNT :: sDONE :: Nil = Enum(3)
        val state = RegInit(sIDLE)
        
        switch (state){
            is (sIDLE){
                when (io.in){
                    state := sCNT
                }
            }
            is (sCNT){
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
            
        when (io.in & state === sIDLE){
            cnt := 0.U
            lim := LFSR(latency)
        }

        when (state === sCNT){
            cnt := cnt + 1.U
        }
        io.out  := state === sDONE
    }
    else{
        io.out  := io.in
    }
}

object LatencyPipeRis{
    def apply(in: chisel3.Bool, latency:Int) = {
        val pipe = Module(new LatencyPipeRis(latency))
        pipe.io.in := in
        pipe.io.out
    }
}
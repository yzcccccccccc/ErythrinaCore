package bus.axi4

import chisel3._
import chisel3.util._
import erythcore.ErythrinaDefault
import utils._

class AXI4DelayPipe[T <: AXI4Lite](_type: T = new AXI4) extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val master  = Flipped(_type)
        val slave   = _type
    })

    if (LATENCY > 0){
        LatencyPipe(io.master.ar, io.slave.ar, LATENCY)
        LatencyPipe(io.master.aw, io.slave.aw, LATENCY)
        LatencyPipe(io.slave.r, io.master.r, LATENCY)
        LatencyPipe(io.slave.b, io.master.b, LATENCY)
        LatencyPipe(io.master.w, io.slave.w, LATENCY)
    }
    else{
        io.master   <> io.slave
    }
}

object DelayConnect{
    def apply(master: AXI4Lite, slave: AXI4Lite) = {
       val delaypipe = Module(new AXI4DelayPipe)
       delaypipe.io.master  <> master
       delaypipe.io.slave   <> slave
    }
}
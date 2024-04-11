package bus.axi4

import chisel3._
import chisel3.util._
import erythcore.ErythrinaDefault
import utils.LatencyPipeRis

class DelayPipe extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val master  = Flipped(new AXI4Lite)
        val slave   = new AXI4Lite
    })
    io.slave <> io.master

    io.slave.ar.valid  := LatencyPipeRis(io.master.ar.valid, LATENCY)
    io.slave.aw.valid  := LatencyPipeRis(io.master.aw.valid, LATENCY)
    io.slave.w.valid   := LatencyPipeRis(io.master.w.valid, LATENCY)
    io.slave.r.ready   := LatencyPipeRis(io.master.r.ready, LATENCY)
    io.slave.b.ready   := LatencyPipeRis(io.master.b.ready, LATENCY)

    io.master.ar.ready := LatencyPipeRis(io.slave.ar.ready, LATENCY)
    io.master.aw.ready := LatencyPipeRis(io.slave.aw.ready, LATENCY)
    io.master.w.ready  := LatencyPipeRis(io.slave.w.ready, LATENCY)
    io.master.r.valid  := LatencyPipeRis(io.slave.r.valid, LATENCY)
    io.master.b.valid  := LatencyPipeRis(io.slave.b.valid, LATENCY)
}

object DelayConnect{
    def apply(master: AXI4Lite, slave: AXI4Lite) = {
       val delaypipe = Module(new DelayPipe)
       delaypipe.io.master  <> master
       delaypipe.io.slave   <> slave
    }
}
package bus.axi4

import chisel3._
import chisel3.util._
import erythcore.ErythrinaDefault
import utils.LatencyPipeRis

object DelayConnect extends ErythrinaDefault{
    def apply(master: AXI4Lite, slave: AXI4Lite) = {
        slave <> master

        slave.ar.valid  := LatencyPipeRis(master.ar.valid, LATENCY)
        slave.aw.valid  := LatencyPipeRis(master.aw.valid, LATENCY)
        slave.w.valid   := LatencyPipeRis(master.w.valid, LATENCY)
        slave.r.ready   := LatencyPipeRis(master.r.ready, LATENCY)
        slave.b.ready   := LatencyPipeRis(master.b.ready, LATENCY)

        master.ar.ready := LatencyPipeRis(slave.ar.ready, LATENCY)
        master.aw.ready := LatencyPipeRis(slave.aw.ready, LATENCY)
        master.w.ready  := LatencyPipeRis(slave.w.ready, LATENCY)
        master.r.valid  := LatencyPipeRis(slave.r.valid, LATENCY)
        master.b.valid  := LatencyPipeRis(slave.b.valid, LATENCY)
    }
}
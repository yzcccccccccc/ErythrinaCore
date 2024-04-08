package top

import chisel3._
import bus.mem._
import erythcore.{ErythrinaCore, ErythrinaCommit}
import utils.LatencyPipeRis
import erythcore.ErythrinaDefault

class Soc extends Module with ErythrinaDefault{
    val io_commit = IO(new ErythrinaCommit)

    val erythrinacore = Module(new ErythrinaCore)

    val InstMem = Module(new SimpleRamAXI)
    val DataMem = Module(new SimpleRamAXI)

    erythrinacore.io.InstCommit <> io_commit
    
    InstMem.io.clock := clock
    InstMem.io.reset := reset
    erythrinacore.io.mem_port1  <> InstMem.io.port
    InstMem.io.port.ar.valid    := LatencyPipeRis(erythrinacore.io.mem_port1.ar.valid, LATENCY)
    InstMem.io.port.aw.valid    := LatencyPipeRis(erythrinacore.io.mem_port1.aw.valid, LATENCY)
    InstMem.io.port.w.valid     := LatencyPipeRis(erythrinacore.io.mem_port1.w.valid, LATENCY)
    InstMem.io.port.r.ready     := LatencyPipeRis(erythrinacore.io.mem_port1.r.ready, LATENCY)
    InstMem.io.port.b.ready     := LatencyPipeRis(erythrinacore.io.mem_port1.b.ready, LATENCY)
    erythrinacore.io.mem_port1.ar.ready := LatencyPipeRis(InstMem.io.port.ar.ready, LATENCY)
    erythrinacore.io.mem_port1.aw.ready := LatencyPipeRis(InstMem.io.port.aw.ready, LATENCY)
    erythrinacore.io.mem_port1.w.ready  := LatencyPipeRis(InstMem.io.port.w.ready, LATENCY)
    erythrinacore.io.mem_port1.r.valid  := LatencyPipeRis(InstMem.io.port.r.valid, LATENCY)
    erythrinacore.io.mem_port1.b.valid  := LatencyPipeRis(InstMem.io.port.b.valid, LATENCY)


    DataMem.io.clock := clock
    DataMem.io.reset := reset
    erythrinacore.io.mem_port2  <> DataMem.io.port
    DataMem.io.port.ar.valid    := LatencyPipeRis(erythrinacore.io.mem_port2.ar.valid, LATENCY)
    DataMem.io.port.aw.valid    := LatencyPipeRis(erythrinacore.io.mem_port2.aw.valid, LATENCY)
    DataMem.io.port.w.valid     := LatencyPipeRis(erythrinacore.io.mem_port2.w.valid, LATENCY)
    DataMem.io.port.r.ready     := LatencyPipeRis(erythrinacore.io.mem_port2.r.ready, LATENCY)
    DataMem.io.port.b.ready     := LatencyPipeRis(erythrinacore.io.mem_port2.b.ready, LATENCY)
    erythrinacore.io.mem_port2.ar.ready := LatencyPipeRis(DataMem.io.port.ar.ready, LATENCY)
    erythrinacore.io.mem_port2.aw.ready := LatencyPipeRis(DataMem.io.port.aw.ready, LATENCY)
    erythrinacore.io.mem_port2.w.ready  := LatencyPipeRis(DataMem.io.port.w.ready, LATENCY)
    erythrinacore.io.mem_port2.r.valid  := LatencyPipeRis(DataMem.io.port.r.valid, LATENCY)
    erythrinacore.io.mem_port2.b.valid  := LatencyPipeRis(DataMem.io.port.b.valid, LATENCY)

}
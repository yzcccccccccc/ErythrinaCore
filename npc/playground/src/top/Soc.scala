package top

import chisel3._
import bus.mem._
import erythcore.{ErythrinaCore, ErythrinaCommit}
import utils.LatencyPipeRis
import erythcore.ErythrinaDefault
import bus.axi4.AXI4LiteArbiter2x1
import bus.axi4.DelayConnect

class Soc extends Module with ErythrinaDefault{
    val io_commit = IO(new ErythrinaCommit)

    val erythrinacore = Module(new ErythrinaCore)

    

    erythrinacore.io.InstCommit <> io_commit

    if (MARCH == "H"){
        val InstMem = Module(new SimpleRamAXI)
        val DataMem = Module(new SimpleRamAXI)

        InstMem.io.clock := clock
        InstMem.io.reset := reset
        DelayConnect(erythrinacore.io.mem_port1, InstMem.io.port)

        DataMem.io.clock := clock
        DataMem.io.reset := reset
        DelayConnect(erythrinacore.io.mem_port2, DataMem.io.port)
    }

    if (MARCH == "P"){
        val memory  = Module(new SimpleRamAXI)
        val arbiter = Module(new AXI4LiteArbiter2x1)
        
        memory.io.clock := clock
        memory.io.reset := reset
        arbiter.io.in1  <> erythrinacore.io.mem_port1
        arbiter.io.in2  <> erythrinacore.io.mem_port2

        //arbiter.io.out  <> memory.io.port
        DelayConnect(arbiter.io.out, memory.io.port)
    }

}
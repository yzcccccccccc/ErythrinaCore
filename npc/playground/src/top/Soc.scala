package top

import chisel3._
import bus.mem._
import erythcore.{ErythrinaCore, ErythrinaCommit}

class Soc extends Module {
    val io_commit = IO(new ErythrinaCommit)

    val erythrinacore = Module(new ErythrinaCore)

    val InstMem = Module(new SimpleRamAXI)
    val DataMem = Module(new SimpleRamAXI)

    erythrinacore.io.InstCommit <> io_commit
    
    InstMem.io.clock := clock
    InstMem.io.reset := reset
    erythrinacore.io.mem_port1  <> InstMem.io.port

    DataMem.io.clock := clock
    DataMem.io.reset := reset
    erythrinacore.io.mem_port2  <> DataMem.io.port

}
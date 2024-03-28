package top

import chisel3._
import ErythrinaCore._
import bus.mem._

class Soc extends Module {
    val io_commit = IO(new ErythrinaCommit)

    val erythrinacore = Module(new ErythrinaCore)

    val InstMem = Module(new SimpleRam)
    val DataMem = Module(new SimpleRam) 

    erythrinacore.io.InstCommit <> io_commit
    
    InstMem.io.clock := clock
    InstMem.io.reset := reset
    erythrinacore.io.MemReq1 <> InstMem.io.RamReq
    erythrinacore.io.MemResp1 <> InstMem.io.RamResp
    DataMem.io.clock := clock
    DataMem.io.reset := reset
    erythrinacore.io.MemReq2  <> DataMem.io.RamReq
    erythrinacore.io.MemResp2 <> DataMem.io.RamResp
}
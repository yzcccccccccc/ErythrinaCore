package top

import chisel3._
import ErythrinaCore._
import bus.mem._

class Soc extends Module {
    val io_commit = IO(new ErythrinaCommit)

    val erythrinacore = Module(new ErythrinaCore)

    val simMem  = Module(new SimpleRam)

    erythrinacore.io.InstCommit <> io_commit
    
    simMem.io.clock := clock
    simMem.io.reset := reset
    erythrinacore.io.MemReq <> simMem.io.RamReq
    erythrinacore.io.MemResp <> simMem.io.RamResp
}
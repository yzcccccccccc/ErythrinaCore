package top

import chisel3._
import bus.mem._
import erythcore.ErythrinaCommit

class TOP extends Module {
    val io_commit = IO(new ErythrinaCommit)

    val soc = Module(new Soc)
    
    soc.io_commit <> io_commit
}
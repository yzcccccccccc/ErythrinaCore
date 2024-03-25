package top

import chisel3._
import ErythrinaCore._
import bus.mem._

class TOP extends Module {
    val io_commit = IO(new ErythrinaCommit)

    val soc = Module(new Soc)
    
    soc.io_commit <> io_commit
}
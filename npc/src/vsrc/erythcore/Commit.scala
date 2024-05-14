package erythcore

import chisel3._
import chisel3.util._

class CommitWrapperIO extends Bundle{
    val clock   = Input(Clock())
    val reset   = Input(Bool())
    val port    = Flipped(new ErythrinaCommit)
}

class CommitWrapper extends Module{
    val io = IO(new CommitWrapperIO)

    if (!ErythrinaSetting.isSTA){
        val port_r = RegNext(io.port)
        dontTouch(port_r)
    }
}
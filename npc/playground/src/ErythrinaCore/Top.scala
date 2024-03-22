package ErythrinaCore

import chisel3._

class TOP extends Module {
    val io = IO(new ErythrinaCoreIO)

    val erythrinacore = Module(new ErythrinaCore)

    erythrinacore.io <> io
}
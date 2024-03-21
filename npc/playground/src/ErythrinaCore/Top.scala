package ErythrinaCore

import chisel3._

class TOP extends Module {
    val io = IO(new IFUIO)

    val myIFU = Module(new IFU)

    myIFU.io <> io
}
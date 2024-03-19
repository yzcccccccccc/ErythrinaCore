package ErythrinaCore

import chisel3._

class TOP extends Module {
    val io = IO(new ALUIO)

    val myalu = Module(new ALU)

    myalu.io <> io
}
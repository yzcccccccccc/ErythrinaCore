package erythcore.frontend.InstDecode

import chisel3._
import chisel3.util._
import erythcore._

class IDU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val in  = Decoupled(Flipped(new InstFetchIO))
    })

    val decoder1 = Module(new Decoder)
    val decoder2 = Module(new Decoder)

    // Decoders
    decoder1.io.in.instr        := io.in.bits.instr(0)
    decoder1.io.in.pc           := io.in.bits.pc
    decoder1.io.in.instValid    := io.in.bits.instValid

    decoder2.io.in.instr        := io.in.bits.instr(1)
    decoder2.io.in.pc           := io.in.bits.pc + 4.U
    decoder2.io.in.instValid    := io.in.bits.instValid
}
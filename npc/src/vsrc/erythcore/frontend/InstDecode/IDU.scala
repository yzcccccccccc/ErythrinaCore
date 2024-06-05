package erythcore.frontend.InstDecode

import chisel3._
import chisel3.util._
import erythcore._

class IDU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val in  = Flipped(Decoupled(new InstFetchIO))

        // RMT
        val rmt_r = Vec(3, Flipped(new RMTrports))
        val rmt_w = Flipped(new RMTwports)

        // FreeList
        val fl_deq = Flipped(Decoupled(new FLDeqBundle))

        // reorder buffer idx

    })

    // Decoder
    val decoder = Module(new Decoder)
    decoder.io.in.instValid := io.in.valid
    decoder.io.in.instr     := io.in.bits.instr
    decoder.io.in.pc        := io.in.bits.pc
    val dec_out = decoder.io.out
   
    // RMT
    val psrc1 = Wire(UInt(ARFbits.W))
    val psrc2 = Wire(UInt(ARFbits.W))
    val ppdst = Wire(UInt(ARFbits.W))

    io.rmt_r(0).raddr := dec_out.rs1
    io.rmt_r(1).raddr := dec_out.rs2
    io.rmt_r(2).raddr := dec_out.rd

    psrc1 := io.rmt_r(0).rdata
    psrc2 := io.rmt_r(1).rdata
    ppdst := io.rmt_r(2).rdata

    // FreeList
    io.fl_deq.valid := dec_out.rf_wen & dec_out.instValid
    val free_prf    = io.fl_deq.bits.free_prf
}
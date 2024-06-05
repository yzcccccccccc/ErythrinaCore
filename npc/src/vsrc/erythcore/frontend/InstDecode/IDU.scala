package erythcore.frontend.InstDecode

import chisel3._
import chisel3.util._
import erythcore._
import erythcore.backend._

class IDU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val in  = Flipped(Decoupled(new InstFetchIO))

        // RMT
        val rmt_r = Vec(3, Flipped(new RMTrports))
        val rmt_w = Flipped(new RMTwports)

        // FreeList
        val fl_deq = Flipped(Decoupled(new FLDeqBundle))

        // ROB
        val rob_enq = Flipped(Decoupled(new ROBEnq))

        // RegFile
        val rf_rports = Flipped(Vec(2, new RFrports))

        val out = Decoupled(Output(new InstCtrlBlk))
    })

    val instValid   = io.in.valid

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

    // ROB
    val rob_entry   = Wire(new ROBEntry)
    rob_entry.exceptionVec := dec_out.exceptionVec
    rob_entry.instType     := dec_out.instType
    rob_entry.a_rd         := dec_out.rd
    rob_entry.p_rd         := free_prf
    rob_entry.pp_rd        := ppdst
    rob_entry.pc           := io.in.bits.pc
    rob_entry.isDone       := false.B

    io.rob_enq.valid        := dec_out.instValid
    io.rob_enq.bits.entry   := rob_entry
    io.rob_enq.bits.psrc1   := psrc1
    io.rob_enq.bits.psrc2   := psrc2
    val (psrc1_rdy, psrc2_rdy) = (io.rob_enq.bits.rdy1, io.rob_enq.bits.rdy2)

    // RegFile
    io.rf_rports(0).raddr := psrc1
    io.rf_rports(1).raddr := psrc2
    val (psrc1_dat, psrc2_dat) = (io.rf_rports(0).rdata, io.rf_rports(1).rdata)

    // Output
    io.out.bits.basicInfo   := dec_out
    io.out.bits.psrc1       := psrc1
    io.out.bits.psrc2       := psrc2
    io.out.bits.ppdst       := ppdst
    io.out.bits.pdst        := free_prf
    io.out.bits.src1_dat    := psrc1_dat
    io.out.bits.src2_dat    := psrc2_dat
    io.out.bits.rdy1        := psrc1_rdy
    io.out.bits.rdy2        := psrc2_rdy
}
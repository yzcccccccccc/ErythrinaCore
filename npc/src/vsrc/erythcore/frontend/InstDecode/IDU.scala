package erythcore.frontend.InstDecode

import chisel3._
import chisel3.util._
import erythcore._
import erythcore.common._
import erythcore.backend.fu.LSUOpType

class IDU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val in  = Flipped(Decoupled(new InstFetchIO))

        // RMT
        val rmt_r = Vec(3, Flipped(new RMTrports))
        val rmt_w = Flipped(new RMTwports)

        // FreeList
        val fl_deq  = Flipped(Decoupled(new FLDeqBundle))

        // ROB
        val rob_enq     = Flipped(Decoupled(new ROBEnqBundle))
        val rob_query   = Flipped(new ROBQueryBundle)

        // RegFile
        val rf_rports   = Flipped(Vec(2, new RFrports))

        // Store Buffer
        val sb_enq      = Flipped(Decoupled(new SBEnqBundle))

        val out = Decoupled(Output(new InstCtrlBlk))
    })

    val instValid   = io.in.valid

    // Decoder
    val decoder = Module(new Decoder)
    decoder.io.in.instValid := io.in.valid
    decoder.io.in.instr     := io.in.bits.instr
    decoder.io.in.pc        := io.in.bits.pc
    val dec_out = decoder.io.out
   
    // RMT: Reg Map Table, get the mapped prf for arf
    val psrc1 = Wire(UInt(ARFbits.W))
    val psrc2 = Wire(UInt(ARFbits.W))
    val ppdst = Wire(UInt(ARFbits.W))

    io.rmt_r(0).raddr := dec_out.rs1
    io.rmt_r(1).raddr := dec_out.rs2
    io.rmt_r(2).raddr := dec_out.rd

    psrc1 := io.rmt_r(0).rdata
    psrc2 := io.rmt_r(1).rdata
    ppdst := io.rmt_r(2).rdata

    // FreeList: get a unmapped reg
    val sFL_Req :: sFL_Wait :: Nil = Enum(2)
    val fl_state = RegInit(sFL_Req)
    switch(fl_state){
        is(sFL_Req){
            when(io.fl_deq.fire & ~io.out.fire){
                fl_state := sFL_Wait
            }
        }
        is(sFL_Wait){
            when(io.out.fire){
                fl_state := sFL_Req
            }
        }
    }

    io.fl_deq.valid := dec_out.rf_wen & dec_out.instValid & fl_state === sFL_Req
    val free_prf    = io.fl_deq.bits.free_prf
    val free_prf_r  = RegEnable(free_prf, io.fl_deq.fire)
    val prf         = Mux(fl_state === sFL_Wait, free_prf_r, free_prf)

    // ROB: Allocate a entry in ROB
    val rob_entry   = Wire(new ROBEntry)
    rob_entry.exceptionVec  := dec_out.exceptionVec
    rob_entry.instType      := dec_out.instType
    rob_entry.a_rd          := dec_out.rd
    rob_entry.p_rd          := free_prf
    rob_entry.pp_rd         := ppdst
    rob_entry.pc            := io.in.bits.pc
    rob_entry.rf_wen        := dec_out.rf_wen
    rob_entry.res           := DontCare
    rob_entry.isDone        := false.B

    val sROB_Req :: sROB_Wait :: Nil = Enum(2)
    val rob_state = RegInit(sROB_Req)
    switch(rob_state){
        is(sROB_Req){
            when(io.rob_enq.fire & ~io.out.fire){
                rob_state := sROB_Wait
            }
        }
        is(sROB_Wait){
            when(io.out.fire){
                rob_state := sROB_Req
            }
        }
    }
    io.rob_enq.valid        := dec_out.instValid & rob_state === sROB_Req
    io.rob_enq.bits.entry   := rob_entry
    val rob_idx_r   = RegEnable(io.rob_enq.bits.rob_idx, io.rob_enq.fire)
    val rob_idx     = Mux(rob_state === sROB_Wait, rob_idx_r, io.rob_enq.bits.rob_idx)

    io.rob_query.psrc1  := psrc1
    io.rob_query.psrc2  := psrc2
    val (rdy1, rdy2)    = (io.rob_query.rdy1, io.rob_query.rdy2)
    val (pause_idx1, pause_idx2) = (io.rob_query.pause_idx1, io.rob_query.pause_idx2)

    // RegFile
    io.rf_rports(0).raddr := psrc1
    io.rf_rports(1).raddr := psrc2
    val (psrc1_dat, psrc2_dat) = (io.rf_rports(0).rdata, io.rf_rports(1).rdata)

    // Store Buffer
    val sb_entry = Wire(new SBEntry)
    sb_entry.stat   := SBstat.pending
    sb_entry.typ    := dec_out.fuOpType
    sb_entry.addr   := DontCare
    sb_entry.data   := DontCare

    val need_sb = LSUOpType.isStore(dec_out.fuOpType) & dec_out.fuType === FuType.lsu

    val sSB_Req :: sSB_Wait :: Nil = Enum(2)
    val sb_state = RegInit(sSB_Req)
    switch(sb_state){
        is(sSB_Req){
            when(io.sb_enq.fire & ~io.out.fire){
                sb_state := sSB_Wait
            }
        }
        is(sSB_Wait){
            when(io.out.fire){
                sb_state := sSB_Req
            }
        }
    }

    io.sb_enq.valid         := need_sb & sb_state === sSB_Req & dec_out.instValid
    io.sb_enq.bits.sb_entry := sb_entry
    val sb_idx_r    = RegEnable(io.sb_enq.bits.sb_idx, io.sb_enq.fire)
    val sb_idx      = Mux(sb_state === sSB_Wait, sb_idx_r, io.sb_enq.bits.sb_idx)

    // Output
    val rob_valid   = io.rob_enq.fire | rob_state === sROB_Wait
    val fl_valid    = ~dec_out.rf_wen | dec_out.rf_wen & (io.fl_deq.fire | fl_state === sFL_Wait)
    io.out.valid    := rob_valid & fl_valid | ~instValid
    io.in.ready     := io.out.ready & io.out.valid

    io.out.bits.pc          := io.in.bits.pc
    io.out.bits.pnpc        := io.in.bits.pnpc
    io.out.bits.basicInfo   := dec_out
    io.out.bits.psrc1       := psrc1
    io.out.bits.psrc2       := psrc2
    io.out.bits.ppdst       := ppdst
    io.out.bits.pdst        := prf
    io.out.bits.src1_dat    := psrc1_dat
    io.out.bits.src2_dat    := psrc2_dat
    io.out.bits.rdy1        := rdy1
    io.out.bits.rdy2        := rdy2
    io.out.bits.pause_rob_idx1 := pause_idx1
    io.out.bits.pause_rob_idx2 := pause_idx2
    io.out.bits.rob_idx     := rob_idx
    io.out.bits.sb_idx      := sb_idx
    io.out.bits.res         := DontCare
}
package erythcore

import chisel3._
import chisel3.util._

import utils._

class WBUIO extends Bundle with WBUtrait{
    val memu_wbu_zip    = Flipped(Decoupled(new MEM_WB_zip))

    // FWD
    val wbu_fwd_zip     = Flipped(new FWD_RESP_zip)

    // RegFile
    val RegWriteIO  = Flipped(new RegFileOUT)

    // Commit(Debug)
    val inst_commit = new ErythrinaCommit
}

class WBU extends Module with WBUtrait{
    val io = IO(new WBUIO)

    io.memu_wbu_zip.ready   := 1.B
    val content_valid   = io.memu_wbu_zip.bits.content_valid

    io.memu_wbu_zip.bits.RegWriteIO <> io.RegWriteIO
    io.RegWriteIO.wen   := io.memu_wbu_zip.bits.RegWriteIO.wen & content_valid
    
    io.inst_commit.pc       := io.memu_wbu_zip.bits.pc
    io.inst_commit.inst     := io.memu_wbu_zip.bits.inst
    io.inst_commit.rf_wen   := io.memu_wbu_zip.bits.RegWriteIO.wen
    io.inst_commit.rf_wdata := io.memu_wbu_zip.bits.RegWriteIO.wdata
    io.inst_commit.rf_waddr := io.memu_wbu_zip.bits.RegWriteIO.waddr
    io.inst_commit.valid    := content_valid
    io.inst_commit.mem_addr := io.memu_wbu_zip.bits.maddr
    io.inst_commit.mem_data := io.memu_wbu_zip.bits.mdata
    io.inst_commit.mem_wen  := io.memu_wbu_zip.bits.mwen
    io.inst_commit.mem_en   := io.memu_wbu_zip.bits.men

    // FWD
    io.wbu_fwd_zip.datasrc  := FwdDataSrc.DONTCARE
    io.wbu_fwd_zip.rd       := io.memu_wbu_zip.bits.RegWriteIO.waddr
    io.wbu_fwd_zip.wdata    := io.memu_wbu_zip.bits.RegWriteIO.wdata
    io.wbu_fwd_zip.wen      := io.memu_wbu_zip.bits.RegWriteIO.wen
    io.wbu_fwd_zip.valid    := 1.B

    if (!ErythrinaSetting.isSTA){
        val isEbreak = io.memu_wbu_zip.bits.exception.isEbreak
        val isUnknown = io.memu_wbu_zip.bits.exception.isUnknown

        val HaltEbreak = Module(new haltEbreak)
        HaltEbreak.io.halt_trigger := RegNext(isEbreak)

        val HaltUnknown = Module(new haltUnknownInst)
        HaltUnknown.io.halt_trigger := RegNext(isUnknown)

        // debug
        val pc_r    = RegNext(io.memu_wbu_zip.bits.pc)
        dontTouch(pc_r)
        val inst_r  = RegNext(io.memu_wbu_zip.bits.inst)
        dontTouch(inst_r)
    }
}

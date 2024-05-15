package erythcore

import chisel3._
import chisel3.util._

import utils._

class WBUIO extends Bundle with WBUtrait{
    val memu_to_wbu    = Flipped(Decoupled(new mem_to_wb_zip))

    // RegFile
    val RegWriteIO  = Flipped(new RegFileOUT)

    // Commit(Debug)
    val inst_commit = new ErythrinaCommit
}

class WBU extends Module with WBUtrait{
    val io = IO(new WBUIO)

    io.memu_to_wbu.ready   := 1.B
    val content_valid   = io.memu_to_wbu.bits.content_valid

    io.memu_to_wbu.bits.RegWriteIO <> io.RegWriteIO
    io.RegWriteIO.wen   := io.memu_to_wbu.bits.RegWriteIO.wen & content_valid
    
    io.inst_commit.pc       := io.memu_to_wbu.bits.pc
    io.inst_commit.inst     := io.memu_to_wbu.bits.inst
    io.inst_commit.rf_wen   := io.memu_to_wbu.bits.RegWriteIO.wen
    io.inst_commit.rf_wdata := io.memu_to_wbu.bits.RegWriteIO.wdata
    io.inst_commit.rf_waddr := io.memu_to_wbu.bits.RegWriteIO.waddr
    io.inst_commit.valid    := content_valid
    io.inst_commit.mem_addr := io.memu_to_wbu.bits.maddr
    io.inst_commit.mem_en   := io.memu_to_wbu.bits.men

    if (!ErythrinaSetting.isSTA){
        val isEbreak = io.memu_to_wbu.bits.exception.isEbreak
        val isUnknown = io.memu_to_wbu.bits.exception.isUnknown

        val HaltEbreak = Module(new haltEbreak)
        HaltEbreak.io.halt_trigger := isEbreak

        val HaltUnknown = Module(new haltUnknownInst)
        HaltUnknown.io.halt_trigger := isUnknown
    }
}

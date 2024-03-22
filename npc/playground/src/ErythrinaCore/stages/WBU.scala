package ErythrinaCore

import chisel3._
import chisel3.util._

import utils._

class WBUIO extends Bundle with WBUtrait{
    val MEMU2WBU    = Flipped(Decoupled(new MEM2WBzip))

    // RegFile
    val RegWriteIO  = Flipped(new RegFileOUT)

    // Commit(Debug)
    val inst_commit = new ErythrinaCommit
}

class WBU extends Module with WBUtrait{
    val io = IO(new WBUIO)

    io.MEMU2WBU.ready   := 1.B

    io.MEMU2WBU.bits.RegWriteIO <> io.RegWriteIO
    io.inst_commit.pc       := io.MEMU2WBU.bits.pc
    io.inst_commit.inst     := io.MEMU2WBU.bits.inst
    io.inst_commit.rf_wen   := io.MEMU2WBU.bits.RegWriteIO.wen
    io.inst_commit.rf_wdata := io.MEMU2WBU.bits.RegWriteIO.wdata
    io.inst_commit.rf_waddr := io.MEMU2WBU.bits.RegWriteIO.waddr
}

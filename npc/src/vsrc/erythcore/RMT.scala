package erythcore

import chisel3._
import chisel3.util._
import erythcore._

/*
    * RMT: Register Mapping Table
    * Entry: | Physical Register |
    * Read Ports    : 3 (for 1 inst renaming)
    * Write Ports   : 1 (for 1 inst renaming)
*/

trait HasRMTDefault extends HasErythDefault{
    val DATA_WIDTH = PRFbits
    val ADDR_WIDTH = ARFbits
}

class RMTrports extends Bundle with HasRMTDefault{
    val raddr   = Input(UInt(ADDR_WIDTH.W))
    val rdata   = Output(UInt(DATA_WIDTH.W))
}

class RMTwports extends Bundle with HasRMTDefault{
    val waddr = Input(UInt(ADDR_WIDTH.W))
    val wdata = Input(UInt(DATA_WIDTH.W))
    val wen   = Input(Bool())
}

class RMT extends Module with HasRMTDefault{
    val io = IO(new Bundle{
        val r = Vec(3, new RMTrports)
        val w = new RMTwports
    })

    val rmt = Mem(NR_ARF, UInt(DATA_WIDTH.W))
    when (reset.asBool){
        for (i <- 0 until NR_ARF){
            rmt(i) := i.U
        }
    }

    for (i <- 0 until 3){
        io.r(i).rdata := rmt(io.r(i).raddr)
    }

    when (io.w.wen){
        rmt(io.w.waddr) := io.w.wdata
    }
}

/* TODO:
    * Architectural RMT
    * Recording the exact mapping
    * Read Ports    : 0
    * Write Ports   : 2 (for 2 inst retiring)
*/
class aRMT extends Module with HasRMTDefault{
    // TBD
}
package erythcore

import chisel3._
import chisel3.util._

class RFrports extends Bundle with HasErythDefault{
    val raddr   = Input(UInt(PRFbits.W))
    val rdata   = Output(UInt(XLEN.W))
}

class RFwports extends Bundle with HasErythDefault{
    val waddr   = Input(UInt(PRFbits.W))
    val wdata   = Input(UInt(XLEN.W))
    val wen     = Input(Bool())
}

class RegFile extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val rf_rports = Vec(2, new RFrports)
        val rf_wports = Vec(2, new RFwports)
    })

    val rf = Mem(NR_PRF, UInt(XLEN.W))

    // read
    for (i <- 0 until 2){
        io.rf_rports(i).rdata := rf(io.rf_rports(i).raddr)
    }

    // write
    for (i <- 0 until 2){
        when (io.rf_wports(i).wen){
            rf(io.rf_wports(i).waddr) := io.rf_wports(i).wdata
        }
    }
}
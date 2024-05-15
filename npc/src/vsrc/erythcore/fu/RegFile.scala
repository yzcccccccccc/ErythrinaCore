package erythcore

import chisel3._
import chisel3.util._

trait RegTrait extends ErythrinaDefault{
    val RegopLen    = 2
    val RegNum          = 32
    val RegAddrLen      = 5         // log(32)
}

// RegFile
class RegFileIN extends Bundle with RegTrait{
    // 2 Read Ports
    val raddr1  = Input(UInt(RegAddrLen.W))
    val rdata1  = Output(UInt(XLEN.W))
    val raddr2  = Input(UInt(RegAddrLen.W))
    val rdata2  = Output(UInt(XLEN.W))
}

class RegFileOUT extends Bundle with RegTrait{
    // 1 Write Ports
    val waddr   = Input(UInt(RegAddrLen.W))
    val wdata   = Input(UInt(XLEN.W))
    val wen     = Input(Bool())
}

class RegFileIO extends Bundle with RegTrait{
    val rf_rport  = new RegFileIN
    val rf_wport = new RegFileOUT
}

class RegFile extends Module with RegTrait {
    val io = IO(new RegFileIO)

    val RegArray = RegInit(VecInit(Seq.fill(RegNum)(0.U(XLEN.W))))

    when (io.rf_wport.wen){
        RegArray(io.rf_wport.waddr) := Mux(io.rf_wport.waddr === 0.U, 0.U, io.rf_wport.wdata)
    }

    io.rf_rport.rdata1 := RegArray(io.rf_rport.raddr1)
    io.rf_rport.rdata2 := RegArray(io.rf_rport.raddr2)
}
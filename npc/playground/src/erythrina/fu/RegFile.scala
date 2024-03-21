package ErythrinaCore

import chisel3._
import chisel3.util._

trait RegTrait extends ErythrinaDefault{
    val RegopLen    = 2
}

// RegFile
class RegFileIO extends Bundle with RegTrait{
    // 2 Read Ports
    val raddr1  = Input(UInt(RegAddrLen.W))
    val rdata1  = Output(UInt(XLEN.W))
    val raddr2  = Input(UInt(RegAddrLen.W))
    val rdata2  = Output(UInt(XLEN.W))

    // 1 Write Ports
    val waddr   = Input(UInt(RegAddrLen.W))
    val wdata   = Input(UInt(XLEN.W))
}

object RegFile extends Module with ErythrinaDefault {
    val io = IO(new RegFileIO)

    val RegArray = RegInit(VecInit(Seq.fill(RegNum)(0.U(XLEN.W))))

    RegArray(io.waddr) := Mux(io.waddr === 0.U, 0.U, io.wdata)

    io.rdata1 := RegArray(io.raddr1)
    io.rdata2 := RegArray(io.raddr2)
}
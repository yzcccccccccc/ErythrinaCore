package ErythrinaCore

import chisel3._
import chisel3.util._

// RegFile
class RegFileIO extends Bundle with ErythrinaDefault{
    // 2 Read Ports
    val raddr1  = Input(UInt(RegAddrLen.W))
    val rdata1  = Output(UInt(XLEN.W))
    val raddr2  = Input(UInt(RegAddrLen.W))
    val rdata2  = Output(UInt(XLEN.W))

    // 1 Write Ports
    val waddr   = Input(UInt(RegAddrLen.W))
    val wdata   = Input(UInt(XLEN.W))
}

class RegFile extends Module with ErythrinaDefault {
    val io = IO(new RegFileIO)

    val RegArray = RegInit(VecInit(Seq.fill(RegNum)(0.U(XLEN.W))))

    RegAddr(io.waddr) := Mux(io.waddr === 0.U, 0.U, io.wdata)

    io.rdata1 := RegArray(io.raddr1)
    io.rdata2 := RegArray(io.raddr2)
}

// IDU!
class IDUIO extends Bundle with IDUtrait{
    val IFU2IDU = Flipped(Decoupled(new IF2IDzip))
    val IDU2EXU = Decoupled(new ID2EXzip)
}

class IDU extends Module with IDUtrait{
    val io = IO(new IDUIO)
}
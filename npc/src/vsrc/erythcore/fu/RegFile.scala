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
    val readIO  = new RegFileIN
    val writeIO = new RegFileOUT
}

class RegFile extends Module with RegTrait {
    val io = IO(new RegFileIO)

    val RegArray = RegInit(VecInit(Seq.fill(RegNum)(0.U(XLEN.W))))

    when (io.writeIO.wen){
        RegArray(io.writeIO.waddr) := Mux(io.writeIO.waddr === 0.U, 0.U, io.writeIO.wdata)
    }

    io.readIO.rdata1 := RegArray(io.readIO.raddr1)
    io.readIO.rdata2 := RegArray(io.readIO.raddr2)
}
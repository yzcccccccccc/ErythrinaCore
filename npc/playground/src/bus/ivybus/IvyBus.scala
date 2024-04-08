package bus.ivybus

import chisel3._
import chisel3.util._

import erythcore.ErythrinaDefault

class IvyBusReq extends Bundle with ErythrinaDefault{
    val wen     = Output(Bool())
    val addr    = Output(UInt(XLEN.W))
    val mask    = Output(UInt(MASKLEN.W))
    val data    = Output(UInt(XLEN.W))
}

class IvyBusResp extends Bundle with ErythrinaDefault{
    val data    = Input(UInt(XLEN.W))
    val rsp     = Input(UInt(RESPLEN.W))
}

class IvyBus extends Bundle{
    val req     = Decoupled(new IvyBusReq)
    val resp    = Flipped(Decoupled(new IvyBusResp))
}
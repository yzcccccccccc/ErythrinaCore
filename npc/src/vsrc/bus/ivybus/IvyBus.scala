package bus.ivybus

import chisel3._
import chisel3.util._

import erythcore._

class IvyBusReq extends Bundle with HasErythDefault{
    val wen     = Output(Bool())
    val addr    = Output(UInt(XLEN.W))
    val mask    = Output(UInt(MASKLEN.W))
    val data    = Output(UInt(XLEN.W))
    val size    = Output(UInt(3.W))
}

class IvyBusResp extends Bundle with HasErythDefault{
    val data    = Input(UInt(XLEN.W))
    val resp    = Input(UInt(RESPLEN.W))
}

class IvyBus extends Bundle{
    val req     = Decoupled(new IvyBusReq)
    val resp    = Flipped(Decoupled(new IvyBusResp))
}
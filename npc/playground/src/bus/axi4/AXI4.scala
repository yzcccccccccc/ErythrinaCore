package bus.axi4

import chisel3._
import chisel3.util._

import erythcore.ErythrinaDefault

// TODO: Currently only support AXI4-Lite youth version
object AXI4LiteParameters extends ErythrinaDefault{
    val datalen     = XLEN
    val addrlen     = XLEN
    val strblen     = datalen / 8
    val resplen     = 2

    def RESP_OKEY   = 0.U(resplen.W)
    def RESP_FAIL   = 1.U(resplen.W)
}

class AXI4LiteA extends Bundle{
    val addr    = Output(UInt(AXI4LiteParameters.addrlen.W))
    // prot?
}

class AXI4LiteR extends Bundle{
    val data    = Input(UInt(AXI4LiteParameters.datalen.W))
    val resp    = Input(UInt(AXI4LiteParameters.resplen.W))
}

class AXI4LiteW extends Bundle{
    val data    = Output(UInt(AXI4LiteParameters.datalen.W))
    val strb    = Output(UInt(AXI4LiteParameters.strblen.W))
}

class AXI4LiteB extends Bundle{
    val resp    = Input(UInt(AXI4LiteParameters.resplen.W))
}

class AXI4Lite extends Bundle{
    val ar  = Decoupled(new AXI4LiteA)
    val r   = Flipped(Decoupled(new AXI4LiteR))
    val aw  = Decoupled(new AXI4LiteA)
    val w   = Decoupled(new AXI4LiteW)
    val b   = Flipped(Decoupled(new AXI4LiteB))
}
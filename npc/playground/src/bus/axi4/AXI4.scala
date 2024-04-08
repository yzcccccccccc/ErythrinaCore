package bus.axi4

import chisel3._
import chisel3.util._

import erythcore.ErythrinaDefault
import utils.LatencyPipeRis

// TODO: Currently only support AXI4-Lite youth version
object AXI4LiteParameters extends ErythrinaDefault{
    val datalen     = XLEN
    val addrlen     = XLEN
    val strblen     = MASKLEN
    val resplen     = RESPLEN

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

// TODO
/*
object AXI4LiteLatency extends ErythrinaDefault{
    def apply(in: AXI4Lite, direct: String, latency:Int = LATENCY) = {
        val port = IO(new AXI4Lite)
        port <> in
        if (direct == "req"){
            
            port.ar.valid   := LatencyPipeRis(in.ar.valid, latency)
            port.aw.valid   := LatencyPipeRis(in.aw.valid, latency)
            port.w.valid    := LatencyPipeRis(in.w.valid, latency)
            port.r.ready    := LatencyPipeRis(in.r.ready, latency)
            port.b.ready    := LatencyPipeRis(in.b.ready, latency)
            port
        }
        port
    }
}
*/
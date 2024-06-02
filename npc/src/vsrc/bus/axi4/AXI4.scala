package bus.axi4

import chisel3._
import chisel3.util._

import erythcore.HasErythDefault

class AXI4LiteParameters extends HasErythDefault{
    val datalen     = XLEN
    val addrlen     = XLEN
    val strblen     = MASKLEN
    val resplen     = RESPLEN

    def RESP_OKAY   = 0.U(resplen.W)
    def RESP_EXOKAY = 1.U(resplen.W)
    def RESP_SLVERR = 2.U(resplen.W)
    def RESP_DECERR = 3.U(resplen.W)
}

object AXI4LiteParameters extends AXI4LiteParameters

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


// AXI4
// TODO Add burst
object AXI4Parameters extends AXI4LiteParameters{
    val idlen       = 4         // id
    val llen        = 8         // len
    val burstlen    = 2         // burst
    val sizelen     = 3         // size
    override val datalen    = 64
    override val strblen    = 8

    def BURST_FIXED = 0.U(burstlen.W)
    def BURST_INCR  = 1.U(burstlen.W)
    def BURST_WRAP  = 2.U(burstlen.W)
}

class AXI4A extends AXI4LiteA{
    val len     = Output(UInt(AXI4Parameters.llen.W))
    val id      = Output(UInt(AXI4Parameters.idlen.W))
    val size    = Output(UInt(AXI4Parameters.sizelen.W))
    val burst   = Output(UInt(AXI4Parameters.burstlen.W))
}

class AXI4R extends AXI4LiteR{
    val last    = Input(Bool())
    val id      = Input(UInt(AXI4Parameters.idlen.W))
    override val data = Input(UInt(AXI4Parameters.datalen.W))
}

class AXI4W extends AXI4LiteW{
    val last    = Output(Bool())
    override val data = Output(UInt(AXI4Parameters.datalen.W))
    override val strb = Output(UInt(AXI4Parameters.strblen.W))
}

class AXI4B extends AXI4LiteB{
    val id      = Input(UInt(AXI4Parameters.idlen.W))
}

class AXI4 extends AXI4Lite{
    override val ar = Decoupled(new AXI4A)
    override val r  = Flipped(Decoupled(new AXI4R))
    override val aw = Decoupled(new AXI4A)
    override val w  = Decoupled(new AXI4W)
    override val b  = Flipped(Decoupled(new AXI4B))
}

trait HasAXI4{
    val ar = Decoupled(new AXI4A)
    val r  = Flipped(Decoupled(new AXI4R))
    val aw = Decoupled(new AXI4A)
    val w  = Decoupled(new AXI4W)
    val b  = Flipped(Decoupled(new AXI4B))
}
package bus.mem

import chisel3._
import chisel3.util._

import erythcore.ErythrinaDefault
import bus.ivybus.IvyBus
import bus.axi4.AXI4Lite
import bus.ivybus.Ivy2AXI4Lite
import utils.LatencyPipeRis

// A Simple Memory Manager (handle Request & Response)

class MemManager2x1IO extends Bundle with ErythrinaDefault{
    // to memory
    val mem     = new IvyBus

    // for IF
    val ifu_in  = Flipped(new IvyBus)

    // for MEM
    val memu_in = Flipped(new IvyBus)
}

class MemManager2x1 extends Module with ErythrinaDefault{
    val io = IO(new MemManager2x1IO)
    // is an arbiter needed here? -> MEMU First!

    // Request
    val MemReqReady = io.mem.req.ready    
    val MemReqValid = io.ifu_in.req.valid | io.memu_in.req.valid
    io.ifu_in.req.ready     := MemReqReady
    io.memu_in.req.ready    := MemReqReady
    
    io.mem.req.valid        := MemReqValid
    io.mem.req.bits.addr    := Mux(io.memu_in.req.valid, io.memu_in.req.bits.addr, io.ifu_in.req.bits.addr)
    io.mem.req.bits.data    := Mux(io.memu_in.req.valid, io.memu_in.req.bits.data, io.ifu_in.req.bits.data)
    
    // Response
    val MemRespReady    = Mux(io.memu_in.req.valid, io.memu_in.resp.ready, io.ifu_in.resp.ready)
    val MemRespValid    = io.mem.resp.valid
    io.mem.resp.ready           := MemRespReady
    io.ifu_in.resp.valid        := MemRespValid
    io.ifu_in.resp.bits.data    := io.mem.resp.bits.data
    io.memu_in.resp.valid       := MemRespValid
    io.memu_in.resp.bits.data   := io.mem.resp.bits.data
}

class MemManager2x2IO extends Bundle with ErythrinaDefault{
    // to memory
    val mem1    = new AXI4Lite
    val mem2    = new AXI4Lite

    // for IF
    val ifu_in  = Flipped(new IvyBus)

    // for MEM
    val memu_in = Flipped(new IvyBus)

}

class MemManager2x2 extends Module with ErythrinaDefault{
    val io = IO(new MemManager2x2IO)

    val ifu_conv    = Module(new Ivy2AXI4Lite)
    val memu_conv   = Module(new Ivy2AXI4Lite)

    ifu_conv.io.in      <> io.ifu_in
    io.mem1             <> ifu_conv.io.out
    //io.mem1.ar.valid    := LatencyPipeRis(ifu_conv.io.out.ar.valid, LATENCY)
    //io.mem1.aw.valid    := LatencyPipeRis(ifu_conv.io.out.aw.valid, LATENCY)
    //io.mem1.w.valid     := LatencyPipeRis(ifu_conv.io.out.w.valid, LATENCY)
    //io.mem1.r.ready     := LatencyPipeRis(ifu_conv.io.out.r.ready, LATENCY)
    //io.mem1.b.ready     := LatencyPipeRis(ifu_conv.io.out.b.ready, LATENCY)

    memu_conv.io.in     <> io.memu_in
    io.mem2             <> memu_conv.io.out
    //io.mem2.ar.valid    := LatencyPipeRis(memu_conv.io.out.ar.valid, LATENCY)
    //io.mem2.aw.valid    := LatencyPipeRis(memu_conv.io.out.aw.valid, LATENCY)
    //io.mem2.w.valid     := LatencyPipeRis(memu_conv.io.out.w.valid, LATENCY)
    //io.mem2.r.ready     := LatencyPipeRis(memu_conv.io.out.r.ready, LATENCY)
    //io.mem2.b.ready     := LatencyPipeRis(memu_conv.io.out.b.ready, LATENCY)

}
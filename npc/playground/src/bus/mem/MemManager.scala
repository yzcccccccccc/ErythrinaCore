package bus.mem

import chisel3._
import chisel3.util._

import erythcore.ErythrinaDefault
import bus.ivybus.IvyBus

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
    val mem1    = new IvyBus
    val mem2    = new IvyBus

    // for IF
    val ifu_in  = Flipped(new IvyBus)

    // for MEM
    val memu_in = Flipped(new IvyBus)
}

class MemManager2x2 extends Module with ErythrinaDefault{
    val io = IO(new MemManager2x2IO)

    io.mem1 <> io.ifu_in
    io.mem2 <> io.memu_in
}
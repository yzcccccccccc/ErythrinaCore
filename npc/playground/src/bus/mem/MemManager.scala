package bus.mem

import chisel3._
import chisel3.util._

import ErythrinaCore._

// A Simple Memory Manager (handle Request & Response)

// Req: MemManager -> mem
class MemReqIO extends Bundle with ErythrinaDefault{
    val addr    = Output(UInt(XLEN.W))
    val mask    = Output(UInt(MaskLEN.W))
    val data    = Output(UInt(XLEN.W))
}

// Resp: mem -> MemManager
class MemRespIO extends Bundle with ErythrinaDefault{
    val data    = Input(UInt(XLEN.W))
}

class MemManagerIO extends Bundle with ErythrinaDefault{
    // to memory
    val MemReq  = Decoupled(new MemReqIO)
    val MemResp = Flipped(Decoupled(new MemRespIO))

    // for IF
    val IFU_Req     = Flipped(Decoupled(new MemReqIO))
    val IFU_Resp    = Decoupled(new MemRespIO)

    // for MEM
    val MEMU_Req    = Flipped(Decoupled(new MemReqIO))
    val MEMU_Resp   = Decoupled(new MemRespIO)
}

class MemManager extends Module with ErythrinaDefault{
    val io = IO(new MemManagerIO)
    // is an arbiter needed here? -> MEMU First!

    // Request
    val MemReqReady = io.MemReq.ready
    val MemReqValid = io.IFU_Req.valid | io.MEMU_Req.valid
    io.MemReq.valid     := MemReqValid
    io.MemReq.bits.addr := Mux(io.MEMU_Req.valid, io.MEMU_Req.bits.addr, io.IFU_Req.bits.addr)
    io.MemReq.bits.mask := Mux(io.MEMU_Req.valid, io.MEMU_Req.bits.mask, io.IFU_Req.bits.mask)

    // Response
    val MemRespReady    = Mux(io.MEMU_Req.valid, io.MEMU_Resp.ready, io.IFU_Resp.ready)
    val MemRespValid    = io.MemResp.valid
    io.MemResp.ready    := MemRespReady

    io.IFU_Resp.valid       := MemRespValid
    io.IFU_Resp.bits.data   := io.MemResp.bits.data
    io.MEMU_Resp.valid      := MemRespValid
    io.MEMU_Resp.bits.data  := io.MemResp.bits.data
    
}
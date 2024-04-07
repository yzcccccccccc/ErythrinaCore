package bus.mem

import chisel3._
import chisel3.util._

import erythcore.ErythrinaDefault

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

class MemManager2x1IO extends Bundle with ErythrinaDefault{
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

class MemManager2x1 extends Module with ErythrinaDefault{
    val io = IO(new MemManager2x1IO)
    // is an arbiter needed here? -> MEMU First!

    // Request
    val MemReqReady = io.MemReq.ready
    val MemReqValid = io.IFU_Req.valid | io.MEMU_Req.valid
    io.IFU_Req.ready    := MemReqReady
    io.MEMU_Req.ready   := MemReqReady
    io.MemReq.valid     := MemReqValid
    io.MemReq.bits.addr := Mux(io.MEMU_Req.valid, io.MEMU_Req.bits.addr, io.IFU_Req.bits.addr)
    io.MemReq.bits.mask := Mux(io.MEMU_Req.valid, io.MEMU_Req.bits.mask, io.IFU_Req.bits.mask)
    io.MemReq.bits.data := io.MEMU_Req.bits.data

    // Response
    val MemRespReady    = Mux(io.MEMU_Req.valid, io.MEMU_Resp.ready, io.IFU_Resp.ready)
    val MemRespValid    = io.MemResp.valid
    io.MemResp.ready    := MemRespReady

    io.IFU_Resp.valid       := MemRespValid
    io.IFU_Resp.bits.data   := io.MemResp.bits.data
    io.MEMU_Resp.valid      := MemRespValid
    io.MEMU_Resp.bits.data  := io.MemResp.bits.data
}

class MemManager2x2IO extends Bundle with ErythrinaDefault{
    // to memory
    val MemReq1  = Decoupled(new MemReqIO)
    val MemResp1 = Flipped(Decoupled(new MemRespIO))

    val MemReq2  = Decoupled(new MemReqIO)
    val MemResp2 = Flipped(Decoupled(new MemRespIO))

    // for IF
    val IFU_Req     = Flipped(Decoupled(new MemReqIO))
    val IFU_Resp    = Decoupled(new MemRespIO)

    // for MEM
    val MEMU_Req    = Flipped(Decoupled(new MemReqIO))
    val MEMU_Resp   = Decoupled(new MemRespIO)
}

class MemManager2x2 extends Module with ErythrinaDefault{
    val io = IO(new MemManager2x2IO)
    io.MemReq1  <> io.IFU_Req
    io.MemResp1 <> io.IFU_Resp
    io.MemReq2  <> io.MEMU_Req
    io.MemResp2 <> io.MEMU_Resp
}
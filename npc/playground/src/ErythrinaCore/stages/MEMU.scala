package ErythrinaCore

import chisel3._
import chisel3.util._

import bus.mem._
import utils._

class MEMUIO extends Bundle with MEMUtrait{
    val EXU2MEMU    = Flipped(Decoupled(new EX2MEMzip))
    val MEMU2WBU    = Decoupled(new MEM2WBzip)

    // memory
    val MEMU_memReq     = Decoupled(new MemReqIO)
    val MEMU_memResp    = Flipped(Decoupled(new MemRespIO))
}

class MEMU extends Module with MEMUtrait{
    val io = IO(new MEMUIO)

    // MemReq
    val addr = io.EXU2MEMU.bits.addr
    io.MEMU_memReq.valid        := 1.B
    io.MEMU_memReq.bits.addr    := Cat(addr(XLEN - 1, 2), 0.asUInt(2.W))        // 4 Bits align?

    // MemResp
    io.MEMU_memResp.ready       := 1.B
    val data = io.MEMU_memResp.bits.data

    // Byte Res
    val ByteRes     = LookupTree(addr(1,0), List(
        "b00".U     -> data(7, 0),
        "b01".U     -> data(15, 8),
        "b10".U     -> data(23, 16),
        "b11".U     -> data(31, 24)
    ))

    // Half Word Res
    val HWordRes    = LookupTree(addr(1, 0), List(
        "b00".U     -> data(15,0),
        "b01".U     -> data(15,0),
        "b10".U     -> data(31,16),
        "b11".U     -> data(31,16)
    ))

    // Load
    val lsuop = io.EXU2MEMU.bits.LSUop
    val LoadRes = LookupTree(lsuop, List(
        LSUop.lb    -> SignExt(ByteRes, XLEN),
        LSUop.lbu   -> ZeroExt(ByteRes, XLEN),
        LSUop.lh    -> SignExt(HWordRes, XLEN),
        LSUop.lhu   -> ZeroExt(HWordRes, XLEN),
        LSUop.lw    -> data
    ))
    
    // Store
}

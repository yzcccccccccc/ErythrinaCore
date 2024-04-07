package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import utils._

class MEMUIO extends Bundle with MEMUtrait{
    val en          = Input(Bool())
    val EXU2MEMU    = Flipped(Decoupled(new EX2MEMzip))
    val MEMU2WBU    = Decoupled(new MEM2WBzip)

    // memory
    val MEMU_memReq     = Decoupled(new MemReqIO)
    val MEMU_memResp    = Flipped(Decoupled(new MemRespIO))
}

class MEMU extends Module with MEMUtrait{
    val io = IO(new MEMUIO)

    io.EXU2MEMU.ready   := 1.B

    // TODO: May be transfered to LSU in the future?

    // MemReq
    val addr = io.EXU2MEMU.bits.addr
    io.MEMU_memReq.valid        := io.EXU2MEMU.bits.LSUop =/= LSUop.nop & io.EXU2MEMU.valid & io.en
    io.MEMU_memReq.bits.addr    := Cat(addr(XLEN - 1, 2), 0.asUInt(2.W))        // 4 Bits align?

    // MemResp
    io.MEMU_memResp.ready       := 1.B
    val ld_data = io.MEMU_memResp.bits.data

    // Byte Res
    val ByteRes     = LookupTree(addr(1,0), List(
        "b00".U     -> ld_data(7, 0),
        "b01".U     -> ld_data(15, 8),
        "b10".U     -> ld_data(23, 16),
        "b11".U     -> ld_data(31, 24)
    ))

    // Half Word Res
    val HWordRes    = LookupTree(addr(1, 0), List(
        "b00".U     -> ld_data(15,0),
        "b01".U     -> ld_data(15,0),
        "b10".U     -> ld_data(31,16),
        "b11".U     -> ld_data(31,16)
    ))

    // Load
    val lsuop = io.EXU2MEMU.bits.LSUop
    val LoadRes = LookupTree(lsuop, List(
        LSUop.lb    -> SignExt(ByteRes, XLEN),
        LSUop.lbu   -> ZeroExt(ByteRes, XLEN),
        LSUop.lh    -> SignExt(HWordRes, XLEN),
        LSUop.lhu   -> ZeroExt(HWordRes, XLEN),
        LSUop.lw    -> ld_data
    ))
    
    // Store
    val mask = LookupTree(lsuop, List(
        LSUop.sb    -> ("b0001".U << addr(1, 0)),
        LSUop.sh    -> ("b0011".U << (addr(1, 0) & "b10".U)),
        LSUop.sw    -> "b1111".U
    ))
    val st_data = LookupTree(lsuop, List(
        LSUop.sb    -> (io.EXU2MEMU.bits.data2store(7, 0) << (addr(1, 0) << 3.U)),      // *8
        LSUop.sh    -> (io.EXU2MEMU.bits.data2store(15, 0) << ((addr(1, 0) & "b10".U) << 3.U)),
        LSUop.sw    -> (io.EXU2MEMU.bits.data2store)
    ))
    io.MEMU_memReq.bits.mask    := mask
    io.MEMU_memReq.bits.data    := st_data

    // to EXU
    //io.EXU2MEMU.ready           := io.MEMU2WBU.valid & io.MEMU2WBU.ready

    // to WBU!
    val isload = LookupTreeDefault(lsuop, false.B, List(
        LSUop.lb    -> true.B,
        LSUop.lbu   -> true.B,
        LSUop.lh    -> true.B,
        LSUop.lhu   -> true.B,
        LSUop.lw    -> true.B
    ))
    io.MEMU2WBU.valid       := (io.MEMU_memResp.fire | ~io.MEMU_memReq.valid)
    io.MEMU2WBU.bits.pc     := io.EXU2MEMU.bits.pc
    io.MEMU2WBU.bits.inst   := io.EXU2MEMU.bits.inst
    io.MEMU2WBU.bits.RegWriteIO.waddr   := io.EXU2MEMU.bits.rd
    io.MEMU2WBU.bits.RegWriteIO.wdata   := Mux(isload, LoadRes, io.EXU2MEMU.bits.addr)
    io.MEMU2WBU.bits.RegWriteIO.wen     := io.EXU2MEMU.bits.rf_wen
}

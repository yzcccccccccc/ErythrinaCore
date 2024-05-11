package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import utils._
import bus.ivybus._

class MEMUIO extends Bundle with MEMUtrait{
    val en          = Input(Bool())
    val EXU2MEMU    = Flipped(Decoupled(new EX2MEMzip))
    val MEMU2WBU    = Decoupled(new MEM2WBzip)

    // memory
    val memu_mem    = new IvyBus

    // perf
    val memu_perf_probe = Flipped(new PerfMEMU)
}

class MEMU extends Module with MEMUtrait{
    val io = IO(new MEMUIO)

    io.EXU2MEMU.ready   := 1.B

    // TODO: May be transfered to LSU in the future?

    // MemReq
    val addr = io.EXU2MEMU.bits.addr
    io.memu_mem.req.valid       := io.EXU2MEMU.bits.LSUop =/= LSUop.nop & io.EXU2MEMU.valid & io.en
    io.memu_mem.req.bits.addr   := addr

    // MemResp
    io.memu_mem.resp.ready      := 1.B
    val ld_data = io.memu_mem.resp.bits.data

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

    val lsuop = io.EXU2MEMU.bits.LSUop
    // Load
    val load_info_lst   = List(               // (res, size)
        LSUop.lb    -> (SignExt(ByteRes, XLEN), "b000".U),
        LSUop.lbu   -> (ZeroExt(ByteRes, XLEN), "b000".U),
        LSUop.lh    -> (SignExt(HWordRes, XLEN), "b001".U),
        LSUop.lhu   -> (ZeroExt(HWordRes, XLEN), "b001".U),
        LSUop.lw    -> (ld_data, "b010".U)
    )
    val LoadRes = LookupTree(lsuop, load_info_lst.map(p => (p._1, p._2._1)))
    val ld_size = LookupTree(lsuop, load_info_lst.map(p => (p._1, p._2._2)))
    
    // Store
    val mask = LookupTreeDefault(lsuop, 0.U, List(
        LSUop.sb    -> ("b0001".U << addr(1, 0)),
        LSUop.sh    -> ("b0011".U << (addr(1, 0) & "b10".U)),
        LSUop.sw    -> "b1111".U
    ))
    val st_data = LookupTree(lsuop, List(
        LSUop.sb    -> (io.EXU2MEMU.bits.data2store(7, 0) << (addr(1, 0) << 3.U)),      // *8
        LSUop.sh    -> (io.EXU2MEMU.bits.data2store(15, 0) << ((addr(1, 0) & "b10".U) << 3.U)),
        LSUop.sw    -> (io.EXU2MEMU.bits.data2store)
    ))
    val st_size = LookupTree(lsuop, List(
        LSUop.sb    -> "b000".U,
        LSUop.sh    -> "b001".U,
        LSUop.sw    -> "b010".U
    ))
    io.memu_mem.req.bits.wen    := mask =/= 0.U
    io.memu_mem.req.bits.mask   := mask
    io.memu_mem.req.bits.data   := st_data

    // Size
    io.memu_mem.req.bits.size   := Mux(mask =/= 0.U, st_size, ld_size)

    // Alignment Check
    val is_byte = LookupTree(lsuop, List(
        LSUop.sb    -> true.B,
        LSUop.lb    -> true.B,
        LSUop.lbu   -> true.B,
        LSUop.sb    -> true.B,
        LSUop.sh    -> false.B,
        LSUop.lh    -> false.B,
        LSUop.lhu   -> false.B,
        LSUop.sw    -> false.B,
        LSUop.lw    -> false.B
    ))
    val is_half = LookupTree(lsuop, List(
        LSUop.sb    -> false.B,
        LSUop.lb    -> false.B,
        LSUop.lbu   -> false.B,
        LSUop.sb    -> false.B,
        LSUop.sh    -> true.B,
        LSUop.lh    -> true.B,
        LSUop.lhu   -> true.B,
        LSUop.sw    -> false.B,
        LSUop.lw    -> false.B
    ))
    val is_word = LookupTree(lsuop, List(
        LSUop.sb    -> false.B,
        LSUop.lb    -> false.B,
        LSUop.lbu   -> false.B,
        LSUop.sb    -> false.B,
        LSUop.sh    -> false.B,
        LSUop.lh    -> false.B,
        LSUop.lhu   -> false.B,
        LSUop.sw    -> true.B,
        LSUop.lw    -> true.B
    ))
    assert(~io.memu_mem.req.valid | (io.memu_mem.req.valid & ((addr(1, 0) === 0.U & is_word) | (addr(0) === 0.U & is_half) | is_byte)), "Unaligned Memory Access!")

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
    val wdata   = RegNext(Mux(isload, LoadRes, io.EXU2MEMU.bits.addr))
    io.MEMU2WBU.valid       := (io.memu_mem.resp.fire | ~io.memu_mem.req.valid)
    io.MEMU2WBU.bits.pc     := io.EXU2MEMU.bits.pc
    io.MEMU2WBU.bits.inst   := io.EXU2MEMU.bits.inst
    io.MEMU2WBU.bits.RegWriteIO.waddr   := io.EXU2MEMU.bits.rd
    io.MEMU2WBU.bits.RegWriteIO.wdata   := wdata
    io.MEMU2WBU.bits.RegWriteIO.wen     := io.EXU2MEMU.bits.rf_wen
    io.MEMU2WBU.bits.maddr  := addr
    io.MEMU2WBU.bits.men    := io.EXU2MEMU.bits.LSUop =/= LSUop.nop & io.EXU2MEMU.valid

    // Perf
    io.memu_perf_probe.ld_data_event := io.memu_mem.resp.fire & isload
    io.memu_perf_probe.st_data_event := io.memu_mem.resp.fire & (~isload & io.EXU2MEMU.bits.LSUop =/= LSUop.nop)
}

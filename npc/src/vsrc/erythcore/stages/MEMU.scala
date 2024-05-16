package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import utils._
import bus.ivybus._

class MEMUIO extends Bundle with MEMUtrait{
    val exu_memu_zip    = Flipped(Decoupled(new EX_MEM_zip))
    val memu_wbu_zip    = Decoupled(new MEM_WB_zip)

    // FWD
    val memu_fwd_zip    = Flipped(new FWD_RESP_zip)

    // memory
    val memu_mem    = new IvyBus

    // perf
    val memu_perf_probe = Flipped(new PerfMEMU)
}

class MEMU extends Module with MEMUtrait{
    val io = IO(new MEMUIO)

    // FSM
    val sIDLE :: sREQ :: sRECV :: Nil = Enum(3)
    val state = RegInit(sIDLE)
    switch (state){
        is (sIDLE){
            when (~reset.asBool){
                state := sREQ
            }
        }
        is (sREQ){
            when (io.memu_mem.req.fire){
                state := sRECV
            }
        }
        is (sRECV){
            when (io.memu_mem.resp.fire){
                state := sREQ
            }
        }
    }

    io.exu_memu_zip.ready   := 1.B

    // TODO: May be transfered to LSU in the future?

    val content_valid   = io.exu_memu_zip.bits.content_valid
    val need_mem_op     = io.exu_memu_zip.bits.LSUop =/= LSUop.nop & content_valid

    // MemReq
    val addr = io.exu_memu_zip.bits.addr_or_res
    io.memu_mem.req.valid       := need_mem_op & state === sREQ
    io.memu_mem.req.bits.addr   := addr

    // MemResp
    io.memu_mem.resp.ready      := state === sRECV
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

    val lsuop = io.exu_memu_zip.bits.LSUop
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
        LSUop.sb    -> (io.exu_memu_zip.bits.data2store(7, 0) << (addr(1, 0) << 3.U)),      // *8
        LSUop.sh    -> (io.exu_memu_zip.bits.data2store(15, 0) << ((addr(1, 0) & "b10".U) << 3.U)),
        LSUop.sw    -> (io.exu_memu_zip.bits.data2store)
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
    val data_valid = Mux(need_mem_op, io.memu_mem.resp.fire, true.B)
    io.exu_memu_zip.ready           := io.memu_mem.req.ready & data_valid | ~content_valid

    // to WBU!
    val isload = LookupTreeDefault(lsuop, false.B, List(
        LSUop.lb    -> true.B,
        LSUop.lbu   -> true.B,
        LSUop.lh    -> true.B,
        LSUop.lhu   -> true.B,
        LSUop.lw    -> true.B
    ))
    val wdata   = RegNext(Mux(isload, LoadRes, io.exu_memu_zip.bits.addr_or_res))
    io.memu_wbu_zip.valid       := data_valid
    io.memu_wbu_zip.bits.content_valid   := content_valid
    io.memu_wbu_zip.bits.pc              := io.exu_memu_zip.bits.pc
    io.memu_wbu_zip.bits.inst            := io.exu_memu_zip.bits.inst
    io.memu_wbu_zip.bits.RegWriteIO.waddr   := io.exu_memu_zip.bits.rd
    io.memu_wbu_zip.bits.RegWriteIO.wdata   := wdata
    io.memu_wbu_zip.bits.RegWriteIO.wen     := io.exu_memu_zip.bits.rf_wen
    io.memu_wbu_zip.bits.maddr   := addr
    io.memu_wbu_zip.bits.men     := need_mem_op
    io.memu_wbu_zip.bits.exception := io.exu_memu_zip.bits.exception

    // to FWD
    io.memu_fwd_zip.datasrc := FwdDataSrc.DONTCARE
    io.memu_fwd_zip.rd      := io.exu_memu_zip.bits.rd
    io.memu_fwd_zip.wdata   := wdata
    io.memu_fwd_zip.wen     := io.exu_memu_zip.bits.rf_wen
    io.memu_fwd_zip.valid   := data_valid


    // Perf
    val has_mem_req_fire = Reg(Bool())
    when (io.memu_mem.req.fire) {
        has_mem_req_fire := true.B
    }
    when (io.memu_mem.resp.fire) {
        has_mem_req_fire := false.B
    }

    io.memu_perf_probe.ld_data_event := io.memu_mem.resp.fire & isload
    io.memu_perf_probe.st_data_event := io.memu_mem.resp.fire & (~isload & io.exu_memu_zip.bits.LSUop =/= LSUop.nop)
    io.memu_perf_probe.wait_req_event := io.memu_mem.req.valid & ~io.memu_mem.req.ready & io.memu_wbu_zip.bits.men
    io.memu_perf_probe.wait_resp_event := ~io.memu_mem.resp.valid & io.memu_mem.resp.ready & ~io.memu_mem.req.valid & io.memu_wbu_zip.bits.men & has_mem_req_fire
}

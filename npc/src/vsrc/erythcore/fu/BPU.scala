package erythcore

import chisel3._
import chisel3.util._

import utils._

trait BPUtrait extends ErythrinaDefault{
    // Assume every instruction is 4 bytes aligned

    val BPUopLEN = 4

    val BHVidxLEN   = 6
    val BHVtagLEN   = 24
    val BHVbitsLEN  = 2
    val BHVtarLEN   = 30
}

object BPUop{
    def nop     = "b0000".U
    def jal     = "b0001".U
    def beq     = "b0010".U
    def bne     = "b0011".U
    def blt     = "b0100".U
    def bge     = "b0101".U
    def bltu    = "b0110".U
    def bgeu    = "b0111".U
    def jalr    = "b1000".U
    def csr     = "b1001".U     // ecall, ebreak, ret
}

class IFU_BPU_zip extends Bundle with BPUtrait{
    val pc      = Input(UInt(XLEN.W))
    val pred_pc = Output(UInt(XLEN.W))
}

class IDU_BPU_zip extends Bundle with BPUtrait{
    val content_valid = Input(Bool())
    val pc      = Input(UInt(XLEN.W))
    val src1    = Input(UInt(XLEN.W))
    val src2    = Input(UInt(XLEN.W))
    val bpuop   = Input(UInt(BPUopLEN.W))
}

class EXU_BPU_zip extends Bundle with BPUtrait{
    val aluout  = Flipped(new ALUIO_out)        // in
}

class RedirectInfo extends Bundle with BPUtrait{
    val target      = Output(UInt(XLEN.W))
    val redirect    = Output(Bool())
}

class BHVEntry extends Bundle with BPUtrait{
    val valid   = Bool()
    val tag     = UInt(BHVtagLEN.W)
    val bits    = UInt(BHVbitsLEN.W)
    val tar     = UInt(BHVtarLEN.W)
}
class BHVIO extends Bundle with BPUtrait{
    val update_trigger = Input(Bool())
    val update_idx = Input(UInt(BHVidxLEN.W))
    val update_tag = Input(UInt(BHVtagLEN.W))
    val update_tak = Input(Bool())
    val update_tar = Input(UInt(BHVtarLEN.W))
    val ifu_zip    = new IFU_BPU_zip

    // perf
    val kick_event = Output(Vec(64, Bool()))
}

class BHV extends Module with BPUtrait{
    def get_next_bit(bits: UInt, tak: Bool): UInt = {
        val next_bit = LookupTreeDefault(bits, "b00".U, Array(
            "b00".U -> Mux(tak, "b01".U, "b00".U),
            "b01".U -> Mux(tak, "b11".U, "b00".U),
            "b10".U -> Mux(tak, "b11".U, "b01".U),
            "b11".U -> Mux(tak, "b11".U, "b10".U)
        ))
        next_bit
    }

    val io = IO(new BHVIO)

    val bhv = RegInit(VecInit(Seq.fill(1 << BHVidxLEN)(0.U.asTypeOf(new BHVEntry))))

    /*--------------- update ---------------*/
    when (io.update_trigger){
        bhv(io.update_idx).valid := true.B
        when (bhv(io.update_idx).tag === io.update_tag){
            bhv(io.update_idx).bits := get_next_bit(bhv(io.update_idx).bits, io.update_tak)
            when (io.update_tak){
                bhv(io.update_idx).tar  := io.update_tar
            }
        }.otherwise{
            when (io.update_tak){
                bhv(io.update_idx).bits := "b10".U
                bhv(io.update_idx).tag  := io.update_tag
                bhv(io.update_idx).tar  := io.update_tar
            }.otherwise{
                bhv(io.update_idx).bits := "b01".U
            }
        }
    }

    /*--------------- predict ---------------*/
    val idx = io.ifu_zip.pc(BHVidxLEN + 1, 2)
    val tag = io.ifu_zip.pc(XLEN - 1, BHVidxLEN + 2)

    val snpc = io.ifu_zip.pc + 4.U
    val dnpc = Mux(bhv(idx).bits(1), Cat(bhv(idx).tar, 0.U(2.W)), io.ifu_zip.pc + 4.U)
    val pred_hit = bhv(idx).valid & (bhv(idx).tag === tag)

    io.ifu_zip.pred_pc := Mux(pred_hit, dnpc, snpc)

    /* ------------- kick event ------------ */
    for (i <- 0 until 64){
        io.kick_event(i) := Mux(io.update_idx === i.U, bhv(io.update_idx).tag =/= io.update_tag & io.update_trigger & ~io.update_tak, false.B)
    }
}

class BPUIO extends Bundle with BPUtrait{
    val idu_bpu_trigger  = Input(Bool())
    val exu_bpu_trigger  = Input(Bool())
    val ifu_bpu_zip      = new IFU_BPU_zip
    val idu_bpu_zip      = new IDU_BPU_zip
    val exu_bpu_zip      = new EXU_BPU_zip
    val csr_bpu_zip      = Flipped(new CSR_BPU_zip)
    val IF_Redirect = new RedirectInfo
    val ID_Redirect = new RedirectInfo      // to be used when pipeline is implemented

    // perf
    val bpu_perf_probe = Flipped(new PerfBPU)
}

// TODO: cut into 2 stages pipeline? [decode|pc+imm]
class BPU extends Module with BPUtrait{
    val io      = IO(new BPUIO)
    
    val pc_r    = RegEnable(io.idu_bpu_zip.pc, io.idu_bpu_trigger)
    val src1_r  = RegEnable(io.idu_bpu_zip.src1, io.idu_bpu_trigger)
    val src2_r  = RegEnable(io.idu_bpu_zip.src2, io.idu_bpu_trigger)
    val bpuop_r = RegEnable(io.idu_bpu_zip.bpuop, io.idu_bpu_trigger)

    /*---------- Stage 2 ----------*/
    val bpuop   = bpuop_r
    val tar_pc  = Mux(bpuop === BPUop.csr, io.csr_bpu_zip.target_pc, src1_r + src2_r)
    val snpc    = Mux(io.idu_bpu_zip.content_valid, io.idu_bpu_zip.pc, io.ifu_bpu_zip.pc)

    val alu_zero = io.exu_bpu_zip.aluout.zero
    val dnpc    = LookupTree(bpuop, List(
        BPUop.jal   -> (src1_r + src2_r),
        BPUop.jalr  -> Cat((src1_r + src2_r)(XLEN - 1, 1), 0.B),
        BPUop.beq   -> Mux(alu_zero, tar_pc, pc_r + 4.U),
        BPUop.bne   -> Mux(~alu_zero, tar_pc, pc_r + 4.U),
        BPUop.blt   -> Mux(~alu_zero, tar_pc, pc_r + 4.U),
        BPUop.bge   -> Mux(alu_zero, tar_pc, pc_r + 4.U),
        BPUop.bltu  -> Mux(~alu_zero, tar_pc, pc_r + 4.U),
        BPUop.bgeu  -> Mux(alu_zero, tar_pc, pc_r + 4.U),
        BPUop.csr   -> io.csr_bpu_zip.target_pc
    ))
    val redirect = Mux(bpuop === BPUop.nop, false.B, dnpc =/= snpc)

    // BHV
    val bhv = Module(new BHV)
    bhv.io.update_trigger   := io.exu_bpu_trigger & (bpuop =/= BPUop.nop)
    bhv.io.update_idx       := pc_r(BHVidxLEN + 1, 2)
    bhv.io.update_tag       := pc_r(XLEN - 1, BHVidxLEN + 2)
    bhv.io.update_tak       := dnpc =/= pc_r + 4.U
    bhv.io.update_tar       := dnpc(XLEN - 1, 2)
    bhv.io.ifu_zip <> io.ifu_bpu_zip

    // to IF & ID
    io.IF_Redirect.redirect := redirect
    io.IF_Redirect.target   := dnpc
    io.ID_Redirect.redirect := redirect
    io.ID_Redirect.target   := dnpc

    // perf
    io.bpu_perf_probe.hit_event     := dnpc === snpc & bpuop =/= BPUop.nop
    io.bpu_perf_probe.miss_event    := dnpc =/= snpc & bpuop =/= BPUop.nop
    io.bpu_perf_probe.kick_event    := bhv.io.kick_event
}

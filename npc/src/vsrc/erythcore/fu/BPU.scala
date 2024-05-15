package erythcore

import chisel3._
import chisel3.util._

import utils._

trait BPUtrait extends ErythrinaDefault{
    val BPUopLEN = 4
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

class IDU_BPU_zip extends Bundle with BPUtrait{
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

class BPUIO extends Bundle with BPUtrait{
    val idu_bpu_zip     = new IDU_BPU_zip
    val exu_bpu_zip      = new EXU_BPU_zip
    val csr_bpu_zip      = Flipped(new CSR_BPU_zip)
    val IF_Redirect = new RedirectInfo
    val ID_Redirect = new RedirectInfo      // to be used when pipeline is implemented
}

// TODO: cut into 2 stages pipeline? [decode|pc+imm]
class BPU extends Module with BPUtrait{
    val io      = IO(new BPUIO)

    val bpuop   = io.idu_bpu_zip.bpuop
    val tar_pc  = Mux(bpuop === BPUop.csr, io.csr_bpu_zip.target_pc, io.idu_bpu_zip.src1 + io.idu_bpu_zip.src2)
    val dnpc    = Mux(bpuop === BPUop.jalr, Cat(tar_pc(XLEN - 1, 1), 0.B), tar_pc);
    val snpc    = io.idu_bpu_zip.pc + 4.U

    val redirect = LookupTree(bpuop, List(
        BPUop.nop   -> 0.B,
        BPUop.jal   -> (dnpc =/= snpc),
        BPUop.jalr  -> (dnpc =/= snpc),
        BPUop.beq   -> io.exu_bpu_zip.aluout.zero,
        BPUop.bne   -> ~io.exu_bpu_zip.aluout.zero,
        BPUop.blt   -> ~io.exu_bpu_zip.aluout.zero,
        BPUop.bge   -> io.exu_bpu_zip.aluout.zero,
        BPUop.bltu  -> ~io.exu_bpu_zip.aluout.zero,
        BPUop.bgeu  -> io.exu_bpu_zip.aluout.zero,
        BPUop.csr   -> 1.B
    ))

    // to IF & ID
    io.IF_Redirect.redirect := redirect
    io.IF_Redirect.target   := dnpc
    io.ID_Redirect.redirect := redirect
    io.ID_Redirect.target   := dnpc
}

// TODO: plan to implement a 2-bits guesser !
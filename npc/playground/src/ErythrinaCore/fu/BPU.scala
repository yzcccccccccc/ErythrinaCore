package ErythrinaCore

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

class IDU2BPUzip extends Bundle with BPUtrait{
    val pc      = Input(UInt(XLEN.W))
    val src1    = Input(UInt(XLEN.W))
    val src2    = Input(UInt(XLEN.W))
    val bpuop   = Input(UInt(BPUopLEN.W))
}

class EXU2BPUzip extends Bundle with BPUtrait{
    val aluout  = Flipped(new ALUIO_out)        // in
}

class RedirectInfo extends Bundle with BPUtrait{
    val target      = Output(UInt(XLEN.W))
    val redirect    = Output(Bool())
}

class BPUIO extends Bundle with BPUtrait{
    val ID2BPU      = new IDU2BPUzip
    val EX2BPU      = new EXU2BPUzip
    val CSR2BPU     = Flipped(new CSR2BPUzip)
    val IF_Redirect = new RedirectInfo
    val ID_Redirect = new RedirectInfo      // to be used when pipeline is implemented
}

// TODO: cut into 2 stages pipeline? [decode|pc+imm]
class BPU extends Module with BPUtrait{
    val io      = IO(new BPUIO)

    val bpuop   = io.ID2BPU.bpuop
    val tar_pc  = Mux(bpuop === BPUop.csr, io.CSR2BPU.target_pc, io.ID2BPU.src1 + io.ID2BPU.src2)
    val dnpc    = Mux(bpuop === BPUop.jalr, Cat(tar_pc(XLEN - 1, 1), 0.B), tar_pc);

    val redirect = LookupTree(bpuop, List(
        BPUop.nop   -> 0.B,
        BPUop.jal   -> (dnpc =/= io.ID2BPU.pc),
        BPUop.jalr  -> (dnpc =/= io.ID2BPU.pc),
        BPUop.beq   -> io.EX2BPU.aluout.zero,
        BPUop.bne   -> ~io.EX2BPU.aluout.zero,
        BPUop.blt   -> ~io.EX2BPU.aluout.zero,
        BPUop.bge   -> io.EX2BPU.aluout.zero,
        BPUop.bltu  -> ~io.EX2BPU.aluout.zero,
        BPUop.bgeu  -> io.EX2BPU.aluout.zero,
        BPUop.csr   -> 1.B
    ))

    // to IF & ID
    io.IF_Redirect.redirect := redirect
    io.IF_Redirect.target   := dnpc
    io.ID_Redirect.redirect := redirect
    io.ID_Redirect.target   := dnpc
}

// TODO: plan to implement a 2-bits guesser !
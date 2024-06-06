package erythcore.backend.EXU

import chisel3._
import chisel3.util._
import erythcore._
import erythcore.backend.fu._

class IntExecutor extends Module with HasErythDefault {
    val io = IO(new Bundle {
        val in      = Flipped(Decoupled(new InstCtrlBlk))
        val out     = Decoupled(new InstCtrlBlk)
        val redir   = Output(new RedirectBundle)
    })

    val instblk = io.in.bits

    val (src1_type, src2_type)  = (instblk.basicInfo.rs1Type, instblk.basicInfo.rs2Type)
    val src1_dat    = Mux1H(Seq(
        (src1_type === SrcType.imm) -> instblk.basicInfo.imm,
        (src1_type === SrcType.reg) -> instblk.src1_dat,
        (src1_type === SrcType.pc)  -> instblk.pc
    ))
    val src2_dat    = Mux1H(Seq(
        (src2_type === SrcType.imm) -> instblk.basicInfo.imm,
        (src2_type === SrcType.reg) -> instblk.src2_dat,
        (src2_type === SrcType.pc)  -> instblk.pc
    ))
    val imm_dat     = instblk.basicInfo.imm

    val alu = Module(new ALU)
    val bru = Module(new BRU)

    // Decoupled
    io.in.ready     := io.out.ready & io.out.valid
    io.out.valid    := 1.B

    // alu
    val use_alu     = instblk.basicInfo.fuType === FuType.alu
    val alu_res     = alu.io.res
    alu.io.aluop    := instblk.basicInfo.fuOpType
    alu.io.src1     := src1_dat
    alu.io.src2     := src2_dat
    
    // bru
    val use_bru     = instblk.basicInfo.fuType === FuType.bru
    val bru_res     = bru.io.res
    bru.io.en       := use_bru
    bru.io.bruop    := instblk.basicInfo.fuOpType
    bru.io.src1     := src1_dat
    bru.io.src2     := src2_dat
    bru.io.imm      := imm_dat
    bru.io.pc       := instblk.pc
    bru.io.pnpc     := instblk.pnpc
    io.redir        := bru.io.redir

    // out
    val out_instblk = instblk
    out_instblk.res := Mux(use_alu, alu_res, Mux(use_bru, bru_res, 0.U))
}
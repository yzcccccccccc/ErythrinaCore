package erythcore.backend.EXU

import chisel3._
import chisel3.util._
import erythcore._

class IntExecutor extends Module with HasErythDefault {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new InstCtrlBlk))
        val out = Decoupled(new InstCtrlBlk)
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
}
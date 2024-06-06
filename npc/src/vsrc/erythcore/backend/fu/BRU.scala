package erythcore.backend.fu

import chisel3._
import chisel3.util._
import erythcore._
import utils.LookupTree

object BRUOpType{
    def beq  = "b0000".U
    def bne  = "b0001".U
    def blt  = "b0010".U
    def bge  = "b0011".U
    def bltu = "b0100".U
    def bgeu = "b0101".U
    def jal  = "b1000".U
    def jalr = "b1001".U

    def use_sign(bruop:UInt) = ~bruop(2)
}

class RedirectBundle extends Bundle with HasErythDefault{
    val need_redirect   = Bool()
    val redirect_tar    = UInt(XLEN.W)
}

class BRU extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val en      = Input(Bool())
        val pc      = Input(UInt(XLEN.W))
        val pnpc    = Input(UInt(XLEN.W))
        val src1    = Input(UInt(XLEN.W))
        val src2    = Input(UInt(XLEN.W))
        val imm     = Input(UInt(XLEN.W))
        val bruop   = Input(FuOpType())

        val res     = Output(UInt(XLEN.W))      // for jal & jalr
        val redir   = Output(new RedirectBundle)
    })

    val (src1, src2, bruop)    = (io.src1, io.src2, io.bruop)
    val use_sign    = BRUOpType.use_sign(bruop)

    val src1in  = Cat(use_sign & src1(XLEN - 1), src1)
    val src2in  = Cat(use_sign & src2(XLEN - 1), src2)
    
    val cmp = src1in - src2in

    val j_br_target = io.pc + io.imm                                // branch & jal target
    val jr_target   = Cat((src1 + io.imm)(XLEN - 1, 1), 0.B)        // jalr target
    val seq_target  = io.pc + 4.U
    io.res  := seq_target

    val true_target = MuxLookup(bruop, seq_target)(Seq(
        BRUOpType.beq   -> Mux(cmp === 0.U, j_br_target, seq_target),
        BRUOpType.bne   -> Mux(cmp =/= 0.U, j_br_target, seq_target),
        BRUOpType.blt   -> Mux(cmp(XLEN), j_br_target, seq_target),         // src1in - src2in < 0
        BRUOpType.bge   -> Mux(~cmp(XLEN), j_br_target, seq_target),
        BRUOpType.bltu  -> Mux(src1 < src2, j_br_target, seq_target),
        BRUOpType.bgeu  -> Mux(src1 >= src2, j_br_target, seq_target),
        BRUOpType.jal   -> j_br_target,
        BRUOpType.jalr  -> jr_target
    ))

    io.redir.need_redirect := true_target =/= io.pnpc & io.en
    io.redir.redirect_tar  := true_target
}
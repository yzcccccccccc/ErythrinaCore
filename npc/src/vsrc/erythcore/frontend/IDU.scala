package erythcore.frontend

import chisel3._
import chisel3.util._
import erythcore._
import utils._
import erythcore.backend.fu._

class Decoder extends Module with HasErythDefault{
    val io = IO(new DecoderIO)

    val (instr, pc) = (io.instr, io.pc)
    val instvalid   = io.instvalid

    val decodeList  = ListLookup(instr, Instructions.DecodeDefault, Instructions.DecodeTable)
    val instType :: fuType :: fuOpType  :: Nil = decodeList

    // Decode
    val rs2 = instr(24, 20)
    val rs1 = instr(19, 15)
    val rd  = instr(11, 7)

    // Get Immediate Value
    val immj = Mux(instr(3), SignExt(Cat(instr(31), instr(19, 12), instr(20), instr(30, 21), 0.U(1.W)), XLEN), SignExt(instr(31, 20), XLEN))
    val imm  = LookupTree(instType, List(
        Instructions.InstN -> SignExt(instr(31, 20), XLEN),
        Instructions.InstI -> SignExt(instr(31, 20), XLEN),
        Instructions.InstS -> SignExt(Cat(instr(31, 25), instr(11, 7)), XLEN),
        Instructions.InstB -> SignExt(Cat(instr(31), instr(7), instr(30, 25), instr(11, 8), 0.U(1.W)), XLEN),
        Instructions.InstU -> SignExt(Cat(instr(31, 12), 0.U(12.W)), XLEN),
        Instructions.InstJ -> immj
    ))

    // Normal Src Type (not csr)
    val srcTypeList = List(
        Instructions.InstN -> (SrcType.const, SrcType.const),   // TODO: fix this?
        Instructions.InstB -> (SrcType.reg, SrcType.reg),
        Instructions.InstI -> (SrcType.reg, SrcType.imm),
        Instructions.InstS -> (SrcType.reg, SrcType.imm),
        Instructions.InstU -> (SrcType.imm, SrcType.pc),
        Instructions.InstJ -> (SrcType.pc, SrcType.const)
    )
    val src1Type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._1)))
    val src2Type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._2)))

    // CSR Src Type
    val usei = fuType === FuType.csr && CSROpType.usei(fuOpType)
}
package erythcore.frontend.InstDecode

import chisel3._
import chisel3.util._
import erythcore._
import utils._
import erythcore.backend.fu._

class Decoder extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val in  = new BasicDecodeIn
        val out = new BasicDecodeOut
    })

    val (instr, pc) = (io.in.instr, io.in.pc)
    val instvalid   = io.in.instValid

    val decodeList  = ListLookup(instr, Instructions.DecodeDefault, Instructions.DecodeTable)
    val instType :: fuType :: fuOpType  :: Nil = decodeList

    // Decode
    val (rs2, rs1, rd) = (instr(24, 20), instr(19, 15), instr(11, 7))

    // Get Immediate Value
    val imm  = LookupTree(instType, List(
        Instructions.InstN -> SignExt(instr(31, 20), XLEN),
        Instructions.InstI -> SignExt(instr(31, 20), XLEN),
        Instructions.InstS -> SignExt(Cat(instr(31, 25), instr(11, 7)), XLEN),
        Instructions.InstB -> SignExt(Cat(instr(31), instr(7), instr(30, 25), instr(11, 8), 0.U(1.W)), XLEN),
        Instructions.InstU -> SignExt(Cat(instr(31, 12), 0.U(12.W)), XLEN),
        Instructions.InstJ -> SignExt(Cat(instr(31), instr(19, 12), instr(20), instr(30, 21), 0.U(1.W)), XLEN)
    ))

    // Normal Src Type (not csr)
    val srcTypeList = List(
        Instructions.InstR -> (SrcType.reg, SrcType.reg),
        Instructions.InstI -> (SrcType.reg, SrcType.imm),
        Instructions.InstS -> (SrcType.reg, SrcType.reg),
        Instructions.InstB -> (SrcType.reg, SrcType.reg),
        Instructions.InstU -> (SrcType.pc, SrcType.imm),
        Instructions.InstJ -> (SrcType.pc, SrcType.imm),
        Instructions.InstN -> (SrcType.pc, SrcType.imm)
    )
    val src1Type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._1)))
    val src2Type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._2)))

    // TODO:CSR Src Type

    // Output
    for (i <- 0 until ExceptionSetting.WIDTH){
        io.out.exceptionVec(i) := false.B
    }
    io.out.exceptionVec(ExceptionSetting.isUNI_idx) := instType === Instructions.InstN & instvalid

    io.out.instType := instType
    io.out.fuType   := fuType
    io.out.fuOpType := fuOpType

    io.out.rs1Type  := Mux(instr(6, 0) === "b0110111".U, SrcType.reg, src1Type)      // LUI case
    io.out.rs2Type  := src2Type
    io.out.rs1      := Mux(instr(6, 0) === "b0110111".U, 0.U, rs1)
    io.out.rs2      := rs2
    io.out.rd       := rd
    io.out.rf_wen   := Instructions.need_rf_wen(instType)
    io.out.imm      := imm
}
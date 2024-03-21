package ErythrinaCore

import chisel3._
import chisel3.util._

import utils._

// IDU!
class IDUIO extends Bundle with IDUtrait{
    val IFU2IDU = Flipped(Decoupled(new IF2IDzip))
    val IDU2EXU = Decoupled(new ID2EXzip)
    val RFRead = Flipped(new RegFileIN)
}

class IDU extends Module with IDUtrait{
    val io = IO(new IDUIO)
    
    val instr   = io.IFU2IDU.bits.inst
    val pc      = io.IFU2IDU.bits.pc

    // Decode Instr
    val decodeList = ListLookup(instr, Instructions.decodeDefault, Instructions.decode_table)
    val instType :: aluop :: lsuop :: bpuop :: Nil = decodeList
    val rs1 = instr(24, 20)
    val rs2 = instr(19, 15)
    val rd  = instr(11, 7)

    // RegFile
    io.RFRead.raddr1 := rs1
    io.RFRead.raddr2 := rs2

    // Get src
    val imm = LookupTree(instType, List(
        TypeI   -> SignExt(instr(31, 20), XLEN),
        TypeS   -> SignExt(Cat(instr(31, 25), instr(11, 7)), XLEN),
        TypeB   -> SignExt(Cat(instr(31), instr(7), instr(30, 25), instr(11, 8), 0.U(1.W)), XLEN),
        TypeU   -> SignExt(Cat(instr(31, 12), 0.U(12.W)), XLEN),
        TypeJ   -> SignExt(Cat(instr(31), instr(19, 12), instr(20), instr(30, 21), 0.U(1.W)), XLEN)
    ))
    val rdata1 = io.RFRead.rdata1
    val rdata2 = io.RFRead.rdata2

    // Generate Decode Information
    // TODO: What About TypeN ??
    val srcTypeList = List(             // type -> (src1_type, src2_type)
        TypeI   -> (SrcType.reg, SrcType.imm),
        TypeB   -> (SrcType.reg, SrcType.reg),
        TypeJ   -> (SrcType.pc, SrcType.imm),
        TypeR   -> (SrcType.reg, SrcType.reg),
        TypeS   -> (SrcType.reg, SrcType.imm),
        TypeU   -> (SrcType.imm, SrcType.pc)
    )
    val src1_type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._1)))
    val src2_type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._1)))
    val src1 = LookupTree(src1_type, List(
        SrcType.imm     -> imm,
        SrcType.pc      -> pc,
        SrcType.reg     -> rdata1
    ))
    val src2 = LookupTree(src2_type, List(
        SrcType.imm     -> imm,
        SrcType.pc      -> pc,
        SrcType.reg     -> rdata2
    ))

    val rf_wen = ~(instType === TypeB || instType === TypeS)

    // to EXU!
    io.IDU2EXU.bits.ALUin.src1  := src1
    io.IDU2EXU.bits.ALUin.src2  := src2
    io.IDU2EXU.bits.ALUin.aluop := aluop
    io.IDU2EXU.bits.BPUop       := bpuop
    io.IDU2EXU.bits.LSUop       := lsuop
    io.IDU2EXU.bits.rd          := rd
    io.IDU2EXU.bits.rf_wen      := rf_wen
    io.IDU2EXU.bits.pc          := pc
    io.IDU2EXU.bits.inst        := instr
}
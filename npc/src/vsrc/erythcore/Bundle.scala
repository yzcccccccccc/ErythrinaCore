package erythcore

import chisel3._
import chisel3.util._

class InstFetchIO extends Bundle with HasErythDefault{
    val pc          = Output(UInt(XLEN.W))
    val pnpc        = Output(UInt(XLEN.W))
    val instValid   = Output(Bool())
    val instr       = Output(UInt(XLEN.W))
}

class DecoderIn extends Bundle with HasErythDefault{
    val instr       = Input(UInt(XLEN.W))
    val pc          = Input(UInt(XLEN.W))
    val instValid   = Input(Bool())
}

class BasicDecodeBlk extends Bundle with HasErythDefault{
    val exceptionVec = Vec(ExceptionSetting.WIDTH, Bool())

    val instValid   = Bool()
    val instType    = UInt(4.W)
    val fuType      = FuType()
    val fuOpType    = FuOpType()
    val rs1Type     = SrcType()
    val rs2Type     = SrcType()
    val rs1         = UInt(ARFbits.W)
    val rs2         = UInt(ARFbits.W)
    val rd          = UInt(ARFbits.W)
    val rf_wen      = Bool()
    val imm         = UInt(XLEN.W)

    def hasRd = rf_wen & instValid & rd =/= 0.U
}

class InstCtrlBlk extends Bundle with HasErythDefault{
    /* Extends from BasicDecodeBlk */
    val pc          = UInt(XLEN.W)
    val pnpc        = UInt(XLEN.W)
    
    val basicInfo   = new BasicDecodeBlk

    val psrc1   = UInt(PRFbits.W)
    val psrc2   = UInt(PRFbits.W)
    val ppdst   = UInt(PRFbits.W)
    val pdst    = UInt(PRFbits.W)

    val src1_dat    = UInt(XLEN.W)
    val src2_dat    = UInt(XLEN.W)
    val rdy1    = Bool()
    val rdy2    = Bool()
    val pause_rob_idx1  = UInt(ROBbits.W)       // wait for this rob_idx
    val pause_rob_idx2  = UInt(ROBbits.W)       // wait for this rob_idx

    val res     = UInt(XLEN.W)
    val rob_idx = UInt(ROBbits.W)

    val sb_idx  = UInt(SBbits.W)        // Store buffer
}

class BypassBundle extends Bundle with HasErythDefault{
    val rob_idx = UInt(ROBbits.W)
    val res     = UInt(XLEN.W)
}
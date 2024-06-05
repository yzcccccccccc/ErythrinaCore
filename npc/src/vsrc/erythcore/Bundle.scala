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
}

class InstCtrlBlk extends BasicDecodeBlk{
    /* Extends from BasicDecodeBlk */
    
    val psrc1   = UInt(PRFbits.W)
    val psrc2   = UInt(PRFbits.W)
    val ppdst   = UInt(PRFbits.W)
    val pdst    = UInt(PRFbits.W)

    val src1    = UInt(XLEN.W)
    val rdy1    = Bool()
    val src2    = UInt(XLEN.W)
    val rdy2    = Bool()
}
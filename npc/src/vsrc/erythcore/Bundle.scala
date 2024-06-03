package erythcore

import chisel3._
import chisel3.util._

class InstFetchIO extends Bundle with HasErythDefault{
    val pc          = Output(UInt(XLEN.W))
    val pnpc        = Output(UInt(XLEN.W))
    val instValid   = Output(Bool())
    val instr       = Output(Vec(2, UInt(XLEN.W)))
}

class BasicDecodeIn extends Bundle with HasErythDefault{
    val instr       = Input(UInt(XLEN.W))
    val pc          = Input(UInt(XLEN.W))
    val instValid   = Input(Bool())
}

class BasicDecodeOut extends Bundle with HasErythDefault{
    val exceptionVec = Output(Vec(ExceptionSetting.WIDTH, Bool()))
    
    val instType    = Output(UInt(4.W))
    val fuType      = Output(FuType())
    val fuOpType    = Output(FuOpType())
    val rs1Type     = Output(SrcType())
    val rs2Type     = Output(SrcType())
    val rs1         = Output(UInt(ARFbits.W))
    val rs2         = Output(UInt(ARFbits.W))
    val rd          = Output(UInt(ARFbits.W))
    val rf_wen      = Output(Bool())
    val imm         = Output(UInt(XLEN.W))
}
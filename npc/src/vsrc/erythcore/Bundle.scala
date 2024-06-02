package erythcore

import chisel3._
import chisel3.util._

class InstFetchIO extends Bundle with HasErythDefault{
    val pc          = Output(UInt(XLEN.W))
    val pnpc        = Output(UInt(XLEN.W))
    val instValid   = Output(Bool())
    val instr       = Output(Vec(2, UInt(XLEN.W)))
}

class BasicCtrlBlock extends Bundle with HasErythDefault{
    val instrValid  = Bool()
    val instr       = UInt(XLEN.W)
    val pc          = UInt(XLEN.W)
}

class DecoderIO extends Bundle with HasErythDefault{
    val instvalid   = Input(Bool())
    val instr       = Input(UInt(XLEN.W))
    val pc          = Input(UInt(XLEN.W))
    
    val isUnknown   = Output(Bool())
    val asrc1Type   = Output(SrcType())
    val asrc2Type   = Output(SrcType())
    val asrc1       = Output(UInt(ARFbits.W))
    val asrc2       = Output(UInt(ARFbits.W))
    val adst        = Output(UInt(ARFbits.W))
    val rf_wen      = Output(Bool())
    val imm         = Output(UInt(XLEN.W))
}
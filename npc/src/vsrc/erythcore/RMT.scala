package erythcore

/*
    * RMT: Register Mapping Table
    * Author: yzcc
    * Date: 2024-06-03
*/

import chisel3._
import chisel3.util._

trait HasRMTDefault extends HasErythDefault{
    def ADDR_WIDTH = log2Ceil(NR_PRF)
    def DATA_WIDTH = log2Ceil(NR_ARF)
}

object RTMState{
    def empty   = "b00".U
    def mapped  = "b01".U
    def writebk = "b10".U
    def commit  = "b11".U

    def apply() = UInt(2.W)
}

class RMTEntry extends Bundle with HasRMTDefault{
    val arf     = UInt(DATA_WIDTH.W)
    val state   = RTMState()
}

class RMTIn extends Bundle with HasRMTDefault{
    val raddr_vec = Vec(4, UInt(ADDR_WIDTH.W))
    val waddr_vec = Vec(2, UInt(ADDR_WIDTH.W))
    val wdata_vec = Vec(2, new RMTEntry)
    val wen_vec   = Vec(2, Bool())
}

class RMTOut extends Bundle with HasRMTDefault{
    val rdata_vec = Vec(4, UInt(DATA_WIDTH.W))
}

class RMT extends Module with HasRMTDefault{
    val io = IO(new Bundle {
        val in  = Input(new RMTIn)
        val out = Output(new RMTOut)
    })

    val rmt = RegInit(VecInit(Seq.fill(NR_PRF)(0.U.asTypeOf(new RMTEntry))))

    // Read
    for(i <- 0 until 4){
        io.out.rdata_vec(i) := rmt(io.in.raddr_vec(i)).arf
    }

    // Write
    for(i <- 0 until 2){
        when(io.in.wen_vec(i)){
            rmt(io.in.waddr_vec(i)) := io.in.wdata_vec(i)
        }
    }
}
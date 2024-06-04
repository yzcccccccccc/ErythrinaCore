package erythcore

/*
    * RMT: Register Mapping Table
    * Author: yzcc
    * Date: 2024-06-03
*/

import chisel3._
import chisel3.util._

/*
    | ARF idx |   Mapped PRF idx  |
    -------------------------------
*/

trait HasRMTDefault extends HasErythDefault{
    def ADDR_WIDTH = log2Ceil(NR_PRF)
    def DATA_WIDTH = log2Ceil(NR_ARF)
}

class RMT extends Module with HasRMTDefault{
    val io = IO(new Bundle{
        // 4 read ports
        val raddr   = Input(Vec(4, UInt(ADDR_WIDTH.W)))
        val rdata   = Output(Vec(4, UInt(DATA_WIDTH.W)))

        // 2 write ports
        val wen     = Input(Vec(2, Bool()))
        val waddr   = Input(Vec(2, UInt(ADDR_WIDTH.W)))
        val wdata   = Input(Vec(2, UInt(DATA_WIDTH.W)))
    })

    val rmt = Mem(NR_ARF, UInt(DATA_WIDTH.W))
    when (reset.asBool){
        for (i <- 0 until NR_ARF){
            rmt.write(i.U, i.U)
        }
    }

    // Read
    for (i <- 0 until 4){
        io.rdata(i) := rmt.read(io.raddr(i))
    }

    // Write
    for (i <- 0 until 2){
        when (io.wen(i)){
            rmt.write(io.waddr(i), io.wdata(i))
        }
    }
}
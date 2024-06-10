package erythcore

import chisel3._
import chisel3.util._
import erythcore.common._

class Commiter extends Module with HasErythDefault{
    val io = IO(new Bundle{
        val cmt = Flipped(new ROBDeqBundle)
    })

    if (!ErythSetting.isSTA){
        val rf_wen_vec      = RegInit(VecInit(Seq.fill(2)(false.B)))
        val rf_waddr_vec    = RegInit(VecInit(Seq.fill(2)(0.U(PRFbits.W))))
        val rf_wdata_vec    = RegInit(VecInit(Seq.fill(2)(0.U(XLEN.W))))

        for (i <- 0 until 2){
            when (io.cmt.valid_vec(i)){
                rf_wen_vec(i)   := io.cmt.entry_vec(i).rf_wen
                rf_waddr_vec(i) := io.cmt.entry_vec(i).a_rd
                rf_wdata_vec(i) := io.cmt.entry_vec(i).res
            }
        }

        dontTouch(rf_wen_vec)
        dontTouch(rf_waddr_vec)
        dontTouch(rf_wdata_vec)
    }
}
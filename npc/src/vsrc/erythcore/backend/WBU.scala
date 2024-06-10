package erythcore.backend

import chisel3._
import chisel3.util._
import erythcore._
import erythcore.common._

class WBU extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val in      = Vec(2, Flipped(Decoupled(new InstCtrlBlk)))
        
        // RF
        val rf_wports = Vec(2, Flipped(new RFwports))

        // ROB
        val rob_wb = Vec(2, Valid(new ROBWbBundle))

        // Bypass
        val bypass = Output(Vec(2, Valid(new BypassBundle)))
    })

    for (i <- 0 until 2){
        io.in(i).ready := true.B

        // rf
        io.rf_wports(i).waddr := io.in(i).bits.pdst
        io.rf_wports(i).wdata := io.in(i).bits.res
        io.rf_wports(i).wen   := io.in(i).bits.basicInfo.rf_wen & io.in(i).bits.basicInfo.instValid
    
        // rob
        io.rob_wb(i).valid          := io.in(i).valid & io.in(i).bits.basicInfo.instValid
        io.rob_wb(i).bits.res       := io.in(i).bits.res
        io.rob_wb(i).bits.rob_idx   := io.in(i).bits.rob_idx

        // bypass
        io.bypass(i).valid          := io.in(i).valid & io.in(i).bits.basicInfo.instValid & io.in(i).bits.basicInfo.rf_wen
        io.bypass(i).bits.res       := io.in(i).bits.res
        io.bypass(i).bits.rob_idx   := io.in(i).bits.rob_idx
    }
}
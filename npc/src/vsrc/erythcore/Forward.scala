package erythcore

// handle data forward

import chisel3._
import chisel3.util._

trait FWDtrait extends ErythrinaDefault{
}

object FwdDataSrc{
    def FROM_MEM    = 0.U
    def FROM_ALU    = 1.U
    def FROM_CSR    = 2.U
    def DONTCARE    = 3.U
}

class FWD_REQ_zip extends Bundle with FWDtrait{
    val rs1     = Input(UInt(5.W))
    val rs1_en  = Input(Bool())
    val rs2     = Input(UInt(5.W))
    val rs2_en  = Input(Bool())

    val rs1_occ = Output(Bool())
    val rdata1  = Output(UInt(XLEN.W))
    val rs2_occ = Output(Bool())
    val rdata2  = Output(UInt(XLEN.W))
    val pause   = Output(Bool())
}

class FWD_RESP_zip extends Bundle with FWDtrait{
    val datasrc = Input(UInt(2.W))
    val rd      = Input(UInt(5.W))
    val wen     = Input(Bool())
    val wdata   = Input(UInt(XLEN.W))
    val valid   = Input(Bool())
}

class FWDIO extends Bundle with FWDtrait{
    val req     = new FWD_REQ_zip
    val resp    = Vec(3, new FWD_RESP_zip)
}

class FWD extends Module with FWDtrait{
    val io = IO(new FWDIO)
    
    val rs1_hit_vec = Vec(3, Wire(Bool()))
    val rs2_hit_vec = Vec(3, Wire(Bool()))
    for (i <- 0 until 3){
        rs1_hit_vec(i) := io.req.rs1 === io.resp(i).rd && io.req.rs1_en && io.resp(i).wen
        rs2_hit_vec(i) := io.req.rs2 === io.resp(i).rd && io.req.rs2_en && io.resp(i).wen
    }
    
    val rs1_sel_vec = VecInit(PriorityEncoderOH(rs1_hit_vec))
    val rs2_sel_vec = VecInit(PriorityEncoderOH(rs2_hit_vec))

    io.req.rs1_occ  := rs1_hit_vec.reduce(_ || _)
    io.req.rs2_occ  := rs2_hit_vec.reduce(_ || _)
    io.req.rdata1   := Mux1H(rs1_sel_vec, VecInit(io.resp.map(_.wdata)))
    io.req.rdata2   := Mux1H(rs2_sel_vec, VecInit(io.resp.map(_.wdata)))

    // block logic
    val wait_data_vec  = Wire(Vec(3, Bool()))
    val need_wait   = wait_data_vec.reduce(_ || _)
    for (i <- 0 until 3){
        wait_data_vec(i) := ~io.resp(i).valid && (rs1_sel_vec(i) || rs2_sel_vec(i))
    }
    
    val isLoadUse = (rs1_sel_vec(0) && io.resp(0).datasrc === FwdDataSrc.FROM_MEM) || 
                    (rs2_sel_vec(0) && io.resp(0).datasrc === FwdDataSrc.FROM_MEM)

    io.req.pause := need_wait || isLoadUse
}
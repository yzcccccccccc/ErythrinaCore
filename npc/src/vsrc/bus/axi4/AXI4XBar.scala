package bus.axi4

import chisel3._
import chisel3.util._
import erythcore.ErythrinaDefault

// 1 master, n slave
class AXI4XBar1toN[T <: AXI4Lite](addr_space: List[(UInt, UInt)], _type: T = new AXI4) extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val in  = Flipped(_type)
        val out = Vec(addr_space.length, _type)
    })
    val in = io.in.asInstanceOf[AXI4]

    /*---------- Read Channel ----------*/
    val rd_inflight = RegInit(0.B)
    when (in.ar.valid){
        rd_inflight := 1.B
    }.elsewhen(in.r.fire & in.r.bits.last){
        rd_inflight := 0.B
    }

    val rd_hit_list     = addr_space.map(p => (p._1 <= in.ar.bits.addr) & (in.ar.bits.addr <= p._2))
    val rd_hit_vec      = VecInit(rd_hit_list)
    val rd_hit_vec_r    = RegEnable(rd_hit_vec, in.ar.valid & ~rd_inflight)
    assert(~in.ar.valid | (rd_hit_vec.asUInt.orR), "AXI4XBar1toN: read address out of range")

    val rd_selvec_0     = VecInit(PriorityEncoderOH(rd_hit_vec))
    val rd_selvec_1     = VecInit(PriorityEncoderOH(rd_hit_vec_r))
    val rd_selvec       = Mux(rd_inflight, rd_selvec_1, rd_selvec_0)

    val ar_has_done     = RegInit(0.B)
    when (in.ar.fire){
        ar_has_done := 1.B
    }.elsewhen(in.r.fire & in.r.bits.last){
        ar_has_done := 0.B
    }

    val r_stage         = ar_has_done

    // AR
    in.ar.ready := Mux1H(rd_selvec, io.out.map(_.ar.ready))
    for (i <- 0 until addr_space.length){
        io.out(i).ar.valid := rd_selvec(i) & in.ar.valid
        io.out(i).ar.bits  := in.ar.bits
    }

    // R
    in.r.valid  := Mux1H(rd_selvec, io.out.map(_.r.valid)) & r_stage
    in.r.bits   := Mux1H(rd_selvec, io.out.map(_.r.bits))
    for (i <- 0 until addr_space.length){
        io.out(i).r.ready := rd_selvec(i) & in.r.ready & r_stage
    }

    /*---------- Write Channel ----------*/
    val aw_has_done = RegInit(0.B)
    val w_has_done  = RegInit(0.B)
    when (in.aw.fire){
        aw_has_done := 1.B
    }.elsewhen(in.b.fire){
        aw_has_done := 0.B
    }

    when (in.w.fire & in.w.bits.last){
        w_has_done := 1.B
    }.elsewhen(in.b.fire){
        w_has_done := 0.B
    }

    val b_stage     = w_has_done & aw_has_done

    val wr_inflight = RegInit(0.B)
    when (in.aw.valid){
        wr_inflight := 1.B
    }.elsewhen(in.b.fire){
        wr_inflight := 0.B
    }

    val wr_hitlist  = addr_space.map(p => (p._1 <= in.aw.bits.addr) & (in.aw.bits.addr <= p._2))
    val wr_hitvec   = VecInit(wr_hitlist)
    val wr_hitvec_r = RegEnable(wr_hitvec, in.aw.valid & ~wr_inflight)
    assert(~in.aw.valid | (wr_hitvec.asUInt.orR), "AXI4XBar1toN: write address out of range")

    val wr_selvec_0 = VecInit(PriorityEncoderOH(wr_hitvec))
    val wr_selvec_1 = VecInit(PriorityEncoderOH(wr_hitvec_r))
    val wr_selvec   = Mux(wr_inflight, wr_selvec_1, wr_selvec_0)

    // AW
    in.aw.ready := Mux1H(wr_selvec, io.out.map(_.aw.ready))
    for (i <- 0 until addr_space.length){
        io.out(i).aw.valid := wr_selvec(i) & in.aw.valid
        io.out(i).aw.bits  := in.aw.bits
    }

    // W
    in.w.ready := Mux1H(wr_selvec, io.out.map(_.w.ready))
    for (i <- 0 until addr_space.length){
        io.out(i).w.valid := wr_selvec(i) & in.w.valid
        io.out(i).w.bits  := in.w.bits
    }

    // B
    in.b.valid := Mux1H(wr_selvec, io.out.map(_.b.valid)) & b_stage
    in.b.bits  := Mux1H(wr_selvec, io.out.map(_.b.bits))
    for (i <- 0 until addr_space.length){
        io.out(i).b.ready := wr_selvec(i) & in.b.ready & b_stage
    }
}

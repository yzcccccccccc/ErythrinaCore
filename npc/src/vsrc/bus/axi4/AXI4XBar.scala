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

    val aw_has_fire = Reg(Bool())
    val w_has_fire  = Reg(Bool())

    val r_inflight  = io.in.ar.fire
    val w_inflight  = (io.in.aw.fire | aw_has_fire) & (io.in.w.fire | w_has_fire)

    // FSM
    val sIdle :: sWrite :: sRead :: Nil = Enum(3)
    val state   = RegInit(sIdle)

    switch (state){
        is (sIdle){
            when (r_inflight){
                state   := sRead
            }.elsewhen(w_inflight){
                state   := sWrite
            }
        }
        is (sWrite){
            when (io.in.b.fire){
                state   := sIdle
            }
        }
        is (sRead){
            when (io.in.r.fire){
                state   := sIdle
            }
        }
    }

    // hit check
    val req_addr    = Mux(io.in.ar.valid, io.in.ar.bits.addr, io.in.aw.bits.addr)
    val hit_list    = addr_space.map(p => (p._1 <= req_addr && req_addr <= p._2))
    val hit_vec     = VecInit(hit_list)
    val hit_vec_r   = RegEnable(hit_vec, w_inflight | r_inflight)

    // select
    val select_vec_idle = VecInit(PriorityEncoderOH(hit_vec))
    val select_vec      = VecInit(PriorityEncoderOH(hit_vec_r))

    // AR
    io.in.ar.ready  := Mux1H(select_vec_idle, io.out.map(_.ar.ready)) && state === sIdle
    for (i <- 0 until io.out.length){
        io.out(i).ar.valid  := select_vec_idle(i) && io.in.ar.valid && state === sIdle
        io.out(i).ar.bits   := io.in.ar.bits
    }

    // AW
    io.in.aw.ready  := Mux1H(select_vec_idle, io.out.map(_.aw.ready)) && state === sIdle
    for (i <- 0 until io.out.length){
        io.out(i).aw.valid  := select_vec_idle(i) && io.in.aw.valid && state === sIdle
        io.out(i).aw.bits   := io.in.aw.bits
    }

    // R
    io.in.r.valid   := Mux1H(select_vec, io.out.map(_.r.valid)) && state === sRead
    io.in.r.bits    := Mux1H(select_vec, io.out.map(_.r.bits))
    for (i <- 0 until io.out.length){
        io.out(i).r.ready   := select_vec(i) && io.in.r.ready && state === sRead
    }

    // W
    io.in.w.ready   := Mux1H(select_vec_idle, io.out.map(_.w.ready)) && state === sIdle
    for (i <- 0 until io.out.length){
        io.out(i).w.valid   := select_vec_idle(i) && io.in.w.valid && state === sIdle
        io.out(i).w.bits    := io.in.w.bits
    }

    // B
    io.in.b.valid   := Mux1H(select_vec, io.out.map(_.b.valid)) && state === sWrite
    io.in.b.bits    := Mux1H(select_vec, io.out.map(_.b.bits))
    for (i <- 0 until io.out.length){
        io.out(i).b.ready   := select_vec(i) && io.in.b.ready && state === sWrite
    }
}

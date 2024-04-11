package bus.axi4

import chisel3._
import chisel3.util._
import erythcore.ErythrinaDefault

// 1 master, n slave
class AXI4XBar1toN(addr_space: List[(UInt, UInt)]) extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val in  = Flipped(new AXI4Lite)
        val out = Vec(addr_space.length, new AXI4Lite)
    })

    // FSM
    val sARW :: sR :: sW :: sB :: Nil = Enum(4)
    val state   = RegInit(sARW)

    switch (state){
        is (sARW){
            when (io.in.ar.fire){
                state   := sR
            }.elsewhen(io.in.aw.fire){
                state   := sW
            }
        }
        is (sR){
            when (io.in.r.fire){
                state   := sARW
            }
        }
        is (sW){
            when (io.in.w.fire){
                state   := sB
            }
        }
        is (sB){
            when (io.in.b.fire){
                state   := sARW
            }
        }
    }

    // hit check
    val req_addr    = Mux(io.in.ar.valid, io.in.ar.bits.addr, io.in.aw.bits.addr)
    val hit_list    = addr_space.map(p => (p._1 <= req_addr && req_addr <= p._2))
    val hit_vec     = VecInit(hit_list)
    val hit_vec_r   = RegEnable(hit_vec, io.in.ar.fire | io.in.aw.fire)

    // select
    val select_vec_arw  = VecInit(PriorityEncoderOH(hit_vec))
    val select_vec      = VecInit(PriorityEncoderOH(hit_vec_r))

    // AR
    io.in.ar.ready  := Mux1H(select_vec_arw, io.out.map(_.ar.ready)) && state === sARW
    for (i <- 0 until io.out.length){
        io.out(i).ar.valid  := select_vec_arw(i) && io.in.ar.valid && state === sARW
        io.out(i).ar.bits   := io.in.ar.bits
    }

    // AW
    io.in.aw.ready  := Mux1H(select_vec_arw, io.out.map(_.aw.ready)) && state === sARW
    for (i <- 0 until io.out.length){
        io.out(i).aw.valid  := select_vec_arw(i) && io.in.aw.valid && state === sARW
        io.out(i).aw.bits   := io.in.aw.bits
    }

    // R
    io.in.r.valid   := Mux1H(select_vec, io.out.map(_.r.valid)) && state === sR
    io.in.r.bits    := Mux1H(select_vec, io.out.map(_.r.bits))
    for (i <- 0 until io.out.length){
        io.out(i).r.ready   := select_vec(i) && io.in.r.ready && state === sR
    }

    // W
    io.in.w.ready   := Mux1H(select_vec, io.out.map(_.w.ready)) && state === sW
    for (i <- 0 until io.out.length){
        io.out(i).w.valid   := select_vec(i) && io.in.w.valid && state === sW
        io.out(i).w.bits    := io.in.w.bits
    }

    // B
    io.in.b.valid   := Mux1H(select_vec, io.out.map(_.b.valid)) && state === sB
    io.in.b.bits    := Mux1H(select_vec, io.out.map(_.b.bits))
    for (i <- 0 until io.out.length){
        io.out(i).b.ready   := select_vec(i) && io.in.b.ready && state === sB
    }
}

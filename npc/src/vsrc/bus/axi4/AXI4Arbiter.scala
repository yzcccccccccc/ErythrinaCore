package bus.axi4

import chisel3._
import chisel3.util._

object AXI4MuxDummy {
    def apply[T <: AXI4Lite](slave: T = new AXI4, master: T = new AXI4Lite, cond: Bool)={
        // ar
        master.ar.ready := Mux(cond, slave.ar.ready, 0.B)
        
        // aw
        master.aw.ready := Mux(cond, slave.aw.ready, 0.B)

        // r
        master.r.valid  := Mux(cond, slave.r.valid, 0.B)
        master.r.bits   <> slave.r.bits

        // w
        master.w.ready  := Mux(cond, slave.w.ready, 0.B)

        // b
        master.b.valid  := Mux(cond, slave.b.valid, 0.B)
        master.b.bits   <> slave.b.bits
    }
}

// TODO: improve efficiency
class AXI4ArbiterNto1[T <: AXI4Lite](n:Int, _type: T = new AXI4) extends Module{
    val io = IO(new Bundle {
        val in  = Vec(n, Flipped(_type))
        val out = new AXI4
    })

    val aw_has_fire = Reg(Bool())
    val w_has_fire  = Reg(Bool())
    val ar_has_fire = Reg(Bool())

    // FSM
    val sIdle :: sRead :: sWrite :: Nil = Enum(3)
    val state   = RegInit(sIdle)

    switch (state){
        is (sIdle){
            assert(~(io.out.ar.valid & io.out.aw.valid), "AXI4ArbiterNto1: Both ar and aw are valid!")
            when (io.out.ar.valid){
                state   := sRead
            }.elsewhen(io.out.aw.valid){
                state   := sWrite
            }
        }
        is (sRead){
            when (io.out.r.fire){
                state   := sIdle
            }
        }
        is (sWrite){
            when (io.out.b.fire){
                state   := sIdle
            }
        }
    }

    // Write Mark
    when (io.out.aw.fire){
        aw_has_fire := 1.B
    }

    when (io.out.w.fire){
        w_has_fire  := 1.B
    }

    when (io.out.b.fire){
        aw_has_fire := 0.B
        w_has_fire  := 0.B
    }

    // Read Mark
    when (io.out.ar.fire){
        ar_has_fire := 1.B
    }

    when (io.out.r.fire){
        ar_has_fire := 0.B
    }

    val hit_vec     = VecInit(io.in.map(p => ((p.ar.valid | p.aw.valid) & state === sIdle)))
    val hit_vec_r   = RegEnable(hit_vec, (io.out.ar.valid | io.out.aw.valid) & state === sIdle)

    val select_vec_idle = VecInit(PriorityEncoderOH(hit_vec))
    val select_vec      = VecInit(PriorityEncoderOH(hit_vec_r))

    val select_vec_use  = Mux(state === sIdle, select_vec_idle, select_vec)

    // AR
    io.out.ar.valid := Mux1H(select_vec_use, io.in.map(_.ar.valid))
    io.out.ar.bits  := Mux1H(select_vec_use, io.in.map(_.ar.bits))
    for (i <- 0 until io.in.length){
        io.in(i).ar.ready   := select_vec_use(i) && io.out.ar.ready
    }

    // AW
    io.out.aw.valid := Mux1H(select_vec_use, io.in.map(_.aw.valid))
    io.out.aw.bits  := Mux1H(select_vec_use, io.in.map(_.aw.bits))
    for (i <- 0 until io.in.length){
        io.in(i).aw.ready   := select_vec_use(i) && io.out.aw.ready
    }

    // R
    io.out.r.ready  := Mux1H(select_vec_use, io.in.map(_.r.ready)) && ar_has_fire
    for (i <- 0 until io.in.length){
        io.in(i).r.valid    := select_vec_use(i) && io.out.r.valid && ar_has_fire
        io.in(i).r.bits     := io.out.r.bits
    }

    // W
    io.out.w.valid  := Mux1H(select_vec_use, io.in.map(_.w.valid))
    io.out.w.bits   := Mux1H(select_vec_use, io.in.map(_.w.bits))
    for (i <- 0 until io.in.length){
        io.in(i).w.ready    := select_vec_use(i) && io.out.w.ready
    }

    // B
    io.out.b.ready  := Mux1H(select_vec_use, io.in.map(_.b.ready)) && aw_has_fire && w_has_fire
    for (i <- 0 until io.in.length){
        io.in(i).b.valid    := select_vec_use(i) && io.out.b.valid && aw_has_fire && w_has_fire
        io.in(i).b.bits     := io.out.b.bits
    }
}
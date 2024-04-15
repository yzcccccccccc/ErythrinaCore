package bus.axi4

import chisel3._
import chisel3.util._

object AXI4LiteMuxDummy {
    def apply(slave: AXI4Lite, master: AXI4Lite, cond: Bool)={
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

// TODO Simplify
class AXI4LiteArbiter2x1 extends Module{
    val io = IO(new Bundle {
        val in1 = Flipped(new AXI4Lite)
        val in2 = Flipped(new AXI4Lite)
        val out = new AXI4Lite
    })

    // in1 has higher priority!!!

    // FSM
    val sAREQ :: sPORT1 :: sPORT2 :: Nil = Enum(3)
    val state = RegInit(sAREQ)

    switch (state){
        is (sAREQ){
            when (io.in1.ar.fire | io.in1.aw.fire){
                state   := sPORT1
            }.elsewhen(io.in2.ar.fire | io.in2.aw.fire){
                state   := sPORT2
            }
        }
        is (sPORT1){
            when (io.in1.r.fire | io.in1.b.fire){
                state   := sAREQ
            }
        }
        is (sPORT2){
            when (io.in2.r.fire | io.in2.b.fire){
                state   := sAREQ
            }
        }
    }

    // arbitrate
    val in1_use = (state === sAREQ & (io.in1.ar.valid | io.in1.aw.valid)) | (state === sPORT1)

    // in1
    AXI4LiteMuxDummy(io.out, io.in1, in1_use)

    // in2
    AXI4LiteMuxDummy(io.out, io.in2, ~in1_use)

    // out
    // ar
    io.out.ar.valid     := Mux(in1_use, io.in1.ar.valid, io.in2.ar.valid)
    io.out.ar.bits.addr := Mux(in1_use, io.in1.ar.bits.addr, io.in2.ar.bits.addr)

    // aw
    io.out.aw.valid     := Mux(in1_use, io.in1.aw.valid, io.in2.aw.valid)
    io.out.aw.bits.addr := Mux(in1_use, io.in1.aw.bits.addr, io.in2.ar.bits.addr)

    // r
    io.out.r.ready      := Mux(in1_use, io.in1.r.ready, io.in2.r.ready)

    // w
    io.out.w.valid      := Mux(in1_use, io.in1.w.valid, io.in2.w.valid)
    io.out.w.bits.data  := Mux(in1_use, io.in1.w.bits.data, io.in2.w.bits.data)
    io.out.w.bits.strb  := Mux(in1_use, io.in1.w.bits.strb, io.in2.w.bits.strb)

    // b
    io.out.b.ready      := Mux(in1_use, io.in1.b.ready, io.in2.b.ready)

}
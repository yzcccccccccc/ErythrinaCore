package device

import chisel3._
import chisel3.util._
import bus.axi4.AXI4Lite
import os.stat
import utils.LookupTreeDefault

object AXI4CLINTAddr{
    def rtc_l   = 0xa0000048L.U
    def rtc_h   = 0xa000004cL.U
}

class AXI4CLINTSim extends Module {
    val io = IO(Flipped(new AXI4Lite))

    // FSM
    val sARW :: sR :: sW :: sB ::Nil = Enum(4)
    val state   = RegInit(sARW)

    switch (state){
        is (sARW){
            when (io.ar.fire){
                state   := sR
            }.elsewhen(io.aw.fire){
                state   := sW
            }
        }
        is (sR){
            when (io.r.fire){
                state   := sARW
            }
        }
        is (sW){
            when (io.w.fire){
                state   := sB
            }
        }
        is (sB){
            when (io.b.fire){
                state   := sARW
            }
        }
    }

    // ar
    io.ar.ready := state === sARW
    val addr_r = RegEnable(io.ar.bits.addr, io.ar.fire)

    // r
    val mtime = RegInit(0.U(64.W))
    mtime   := mtime + 1.U
    io.r.valid      := state === sR
    io.r.bits.data  := LookupTreeDefault(addr_r, 0.U, List(
        AXI4CLINTAddr.rtc_l -> mtime(31,0),
        AXI4CLINTAddr.rtc_h -> mtime(63, 32)
    ))
    io.r.bits.resp  := 0.U

    // aw
    assert(~io.aw.valid)
    io.aw.ready := 0.B

    // w
    assert(~io.w.valid)
    io.w.ready  := 0.B

    // b
    io.b.valid      := 0.B
    io.b.bits.resp  := 0.U
}
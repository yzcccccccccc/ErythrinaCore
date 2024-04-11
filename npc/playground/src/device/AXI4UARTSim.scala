package device

import chisel3._
import chisel3.util._
import bus.axi4.AXI4Lite
import utils.MaskExpand

class AXI4UartSim extends Module{
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
    assert(~io.ar.valid)
    io.ar.ready := 0.B

    // aw
    io.aw.ready := state === sARW
    val addr = RegEnable(io.aw.bits.addr, io.aw.fire)

    // r
    io.r.valid      := 0.B
    io.r.bits.data  := 0.U
    io.r.bits.resp  := 0.U

    // w
    io.w.ready      := state === sW
    val data        = io.w.bits.data
    val strb        = MaskExpand(io.w.bits.strb)
    val uart_reg    = Reg(UInt(32.W))
    when (io.w.fire){
        uart_reg    := data & strb | uart_reg & (~strb)
    }

    // b
    io.b.valid      := state === sB
    io.b.bits.resp  := 0.B

    // putchar
    when (io.b.fire){
        printf("%c", uart_reg(7, 0))
    }
}
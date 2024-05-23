package utils

import chisel3._
import chisel3.util._

class haltEbreak extends BlackBox with HasBlackBoxInline{
    val io = IO(new Bundle {
        val halt_trigger    = Input(Bool())
    })
    setInline("halter_Ebreak.sv",
    s"""module haltEbreak(
    |   input   wire halt_trigger
    |);
    |import "DPI-C" function void halt_Ebreak();
    |always @(*) begin
    |   if (halt_trigger) begin
    |       halt_Ebreak();
    |   end
    |end
    |endmodule
    """.stripMargin)
}

class haltUnknownInst extends BlackBox with HasBlackBoxInline{
    val io = IO(new Bundle {
        val halt_trigger    = Input(Bool())
    })
    setInline("halter_UnknownInst.sv",
    s"""module haltUnknownInst(
    |   input   wire halt_trigger
    |);
    |import "DPI-C" function void halt_UnknownINST();
    |always @(*) begin
    |   if (halt_trigger) begin
    |       halt_UnknownINST();
    |   end
    |end
    |endmodule
    """.stripMargin)
}

class haltWatchDog extends BlackBox with HasBlackBoxInline{
    val io = IO(new Bundle{
        val halt_trigger    = Input(Bool())
    })
    setInline("halt_watchdog.sv",
    s"""module haltWatchDog(
    |   input wire halt_trigger
    |);
    |import "DPI-C" function void halt_watchdog();
    |always @(*) begin
    |   if (halt_trigger) begin
    |       halt_watchdog();
    |   end
    |end
    |endmodule
    """.stripMargin)
}

class WatchDog extends Module{
    val io = IO(new Bundle{
        val feed    = Input(Bool())
    })

    val halter  = Module(new haltWatchDog)
    val counter = RegInit(114514.U(32.W))

    when(io.feed){
        counter := 114514.U
    }.otherwise{
        counter := counter - 1.U
    }

    halter.io.halt_trigger  := counter === 0.U & ~reset.asBool
}
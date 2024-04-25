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
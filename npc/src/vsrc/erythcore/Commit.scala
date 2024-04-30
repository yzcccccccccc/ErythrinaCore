package erythcore

import chisel3._
import chisel3.util._

class CommitWrapperIO extends Bundle{
    val clock   = Input(Clock())
    val reset   = Input(Bool())
    val port    = Flipped(new ErythrinaCommit)
}

class CommitWrapper extends BlackBox with HasBlackBoxInline{
    val io = IO(new CommitWrapperIO)
    setInline("commit.sv",
    s"""module CommitWrapper(
    |   input           clock,
    |   input           reset,
    |   input [31:0]    port_pc,
    |   input [31:0]    port_inst,
    |   input           port_rf_wen,
    |   input [4:0]     port_rf_waddr,
    |   input [31:0]    port_rf_wdata,
    |   input [31:0]    port_mem_addr,
    |   input           port_mem_en,
    |   input           port_valid
    |);
    |   reg [31:0]  pc_r, inst_r, rf_wdata_r, mem_addr_r;
    |   reg [4:0]   rf_waddr_r;
    |   reg         rf_wen_r, valid_r, mem_en_r;
    |
    |   always @(posedge clock) begin
    |       pc_r        <= port_pc;
    |       inst_r      <= port_inst;
    |       rf_wdata_r  <= port_rf_wdata;
    |       rf_waddr_r  <= port_rf_waddr;
    |       rf_wen_r    <= port_rf_wen;
    |       valid_r     <= port_valid;
    |       mem_en_r    <= port_mem_en;
    |       mem_addr_r  <= port_mem_addr;
    |   end
    |endmodule
    """.stripMargin
    )
}
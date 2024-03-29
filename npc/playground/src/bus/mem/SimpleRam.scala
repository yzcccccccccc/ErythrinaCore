package bus.mem

import chisel3._
import chisel3.util._

// simulate the bhv of memory?
class SimpleRamIO extends Bundle{
    val clock = Input(Clock())
    val reset = Input(Bool())
    val RamReq  = Flipped(Decoupled(new MemReqIO))
    val RamResp = Decoupled(new MemRespIO)
}

class SimpleRam extends BlackBox with HasBlackBoxInline {
  val io = IO(new SimpleRamIO)
  // TODO: to be fixed (FSM maybe?)
  setInline("simpleram.sv",
    s"""module SimpleRam(
    |   input   clock,
    |   input   reset,
    |   input   RamReq_valid,
    |   output  RamReq_ready,
    |   input   [31:0]  RamReq_bits_addr,
    |   input   [3:0]   RamReq_bits_mask,
    |   input   [31:0]  RamReq_bits_data,
    |   output  RamResp_valid,
    |   input   RamResp_ready,
    |   output  [31:0]  RamResp_bits_data
    |);
    | import "DPI-C" function int mem_read(input int paddr);
    | import "DPI-C" function void mem_write(input int paddr, input bit[3:0] mask, input int data);
    | 
    | reg Resp_valid_r;
    | reg [31:0]  Resp_data_r; 
    |
    | assign RamReq_ready = 1;
    | assign RamResp_valid = Resp_valid_r;
    | assign RamResp_bits_data = Resp_data_r;
    |
    | //  Read & Write
    | always @(posedge clock) begin
    |   if (reset)
    |     Resp_valid_r <= 0;
    |   else begin
    |     if (RamReq_valid & ~Resp_valid_r & RamReq_bits_mask == 4'b0) begin
    |       Resp_valid_r <= 1;
    |       Resp_data_r <= mem_read(RamReq_bits_addr);
    |     end
    |     else
    |       if (RamReq_bits_mask != 4'b0 & RamReq_valid & ~Resp_valid_r) begin
    |         mem_write(RamReq_bits_addr, RamReq_bits_mask, RamReq_bits_data);
    |         Resp_valid_r <= 1;
    |       end
    |       else
    |         Resp_valid_r  <= 0;
    |   end
    | end
    |
    |endmodule
    """.stripMargin);
}
package bus.mem

import  bus.ivybus._

import chisel3._
import chisel3.util._

// simulate the bhv of memory?
class SimpleRamIO extends Bundle{
    val clock = Input(Clock())
    val reset = Input(Bool())
    val port  = Flipped(new IvyBus)
}

class SimpleRam extends BlackBox with HasBlackBoxInline {
  val io = IO(new SimpleRamIO)
  // TODO: to be fixed (FSM maybe?)
  setInline("simpleram.sv",
    s"""module SimpleRam(
    |   input   clock,
    |   input   reset,
    |   input   port_req_valid,
    |   output  port_req_ready,
    |   input   port_req_bits_wen,
    |   input   [31:0]  port_req_bits_addr,
    |   input   [3:0]   port_req_bits_mask,
    |   input   [31:0]  port_req_bits_data,
    |   output  port_resp_valid,
    |   input   port_resp_ready,
    |   output  [31:0]  port_resp_bits_data,
    |   output  [1:0]   port_resp_bits_rsp
    |);
    | import "DPI-C" function int mem_read(input int paddr);
    | import "DPI-C" function void mem_write(input int paddr, input bit[3:0] mask, input int data);
    | 
    | reg Resp_valid_r;
    | reg [31:0]  Resp_data_r; 
    |
    | assign port_req_ready       = 1;
    | assign port_resp_valid      = Resp_valid_r;
    | assign port_resp_bits_data  = Resp_data_r;
    | assign port_resp_bits_rsp   = 0;
    |
    | //  Read & Write
    | always @(posedge clock) begin
    |   if (reset)
    |     Resp_valid_r <= 0;
    |   else begin
    |     if (port_req_valid & ~Resp_valid_r & ~port_req_bits_wen) begin    // read
    |       Resp_valid_r <= 1;
    |       Resp_data_r <= mem_read(port_req_bits_addr);
    |     end
    |     else
    |       if (port_req_bits_wen & port_req_valid & ~Resp_valid_r) begin
    |         mem_write(port_req_bits_addr, port_req_bits_mask, port_req_bits_data);
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
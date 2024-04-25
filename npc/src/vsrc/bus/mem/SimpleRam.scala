package bus.mem

import  bus.ivybus._

import chisel3._
import chisel3.util._
import bus.axi4._
import coursier.core.Latest
import erythcore.ErythrinaDefault

// simulate the bhv of memory?
class SimpleRamIO extends Bundle{
    val clock = Input(Clock())
    val reset = Input(Bool())
    val port  = Flipped(new IvyBus)
}

// Abandon
class SimpleRamAsc extends BlackBox with HasBlackBoxInline{
  val io = IO(new SimpleRamIO)
  setInline("simpleramasc.sv",
    s"""module SimpleRamAsc(
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
    | reg [31:0]  rdata;
    | always @(*) begin
    |   if (port_req_valid) begin
    |     if (port_req_bits_wen) begin
    |       mem_write(port_req_bits_addr, port_req_bits_mask, port_req_bits_data);
    |     end
    |     else begin
    |       rdata = mem_read(port_req_bits_addr);
    |     end
    |   end
    | end
    | 
    | assign port_resp_bits_data  = rdata;
    | assign port_resp_bits_rsp   = 0;
    | assign port_req_ready       = 1;
    | assign port_resp_valid      = 1;
    |endmodule
    """.stripMargin
  )
}

class SimpleRam extends BlackBox with HasBlackBoxInline {
  val io = IO(new SimpleRamIO)
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
    | // FSM
    | reg [1:0] cur_state, nxt_state;
    | localparam  IDLE  = 2'b01;
    | localparam  WAIT  = 2'b10;
    |
    | always @(posedge clock) begin
    |   if (reset)
    |     cur_state <= IDLE;
    |   else
    |     cur_state <= nxt_state;
    | end 
    | 
    | always @(*) begin
    |   case (cur_state)
    |     IDLE: begin
    |       if (port_req_valid)
    |         nxt_state = WAIT;
    |     end
    |     WAIT: begin
    |       if (port_resp_ready)
    |         nxt_state = IDLE;
    |     end
    |     default: nxt_state = IDLE;
    |   endcase
    | end
    |
    | // Read & Write
    | assign port_req_ready   = cur_state == IDLE;
    | assign port_resp_valid  = cur_state == WAIT;
    |
    | reg [31:0] rdata;
    | always @(posedge clock) begin
    |   if (port_req_valid & ~port_req_bits_wen)
    |     rdata <= mem_read(port_req_bits_addr);
    |   if (port_req_valid & port_req_bits_wen)
    |     mem_write(port_req_bits_addr, port_req_bits_mask, port_req_bits_data);
    | end
    | assign port_resp_bits_data  = rdata;
    | assign port_resp_bits_rsp   = 0;
    |
    |endmodule
    """.stripMargin);
}

class SimpleRamAXIIO extends Bundle{
  val clock = Input(Clock())
  val reset = Input(Bool())
  val port  = Flipped(new AXI4)
}

class SimpleRamAXI extends Module with ErythrinaDefault{
  val io = IO(new SimpleRamAXIIO)

  val convt = Module(new AXI42Ivy)
  val ram   = Module(new SimpleRam)

  convt.io.in   <> io.port
  convt.io.out  <> ram.io.port
  //io.port.ar.ready  := LatencyPipeRis(convt.io.in.ar.ready, LATENCY)
  //io.port.aw.ready  := LatencyPipeRis(convt.io.in.aw.ready, LATENCY)
  //io.port.w.ready   := LatencyPipeRis(convt.io.in.w.ready, LATENCY)
  //io.port.r.valid   := LatencyPipeRis(convt.io.in.r.valid, LATENCY)
  //io.port.b.valid   := LatencyPipeRis(convt.io.in.b.valid, LATENCY)

  ram.io.clock  := io.clock
  ram.io.reset  := io.reset
}
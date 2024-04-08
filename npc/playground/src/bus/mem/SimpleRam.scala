package bus.mem

import  bus.ivybus._

import chisel3._
import chisel3.util._
import bus.axi4.AXI4Lite
import coursier.core.Latest
import utils.LatencyPipeRis
import erythcore.ErythrinaDefault

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
    |         if (port_resp_ready)
    |           Resp_valid_r  <= 0;
    |   end
    | end
    |
    |endmodule
    """.stripMargin);
}

class SimpleRamAXIIO extends Bundle{
  val clock = Input(Clock())
  val reset = Input(Bool())
  val port  = Flipped(new AXI4Lite)
}

class SimpleRamAXI extends Module with ErythrinaDefault{
  val io = IO(new SimpleRamAXIIO)

  val convt = Module(new AXI4Lite2Ivy)
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
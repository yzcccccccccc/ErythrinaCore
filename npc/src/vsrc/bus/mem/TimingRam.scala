package bus.mem

import bus.ivybus._

import chisel3._
import chisel3.util._
import bus.axi4._
import erythcore.ErythrinaDefault

class TimingRamIO extends Bundle{
    val clock = Input(Clock())
    val reset = Input(Bool())
    val port  = Flipped(new IvyBus)
}

class TimingRam extends BlackBox with HasBlackBoxInline {
  val io = IO(new TimingRamIO)
  setInline("timingram.sv",
    s"""module TimingRam(
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
    | // read
    | assign port_req_ready = cur_state == IDLE;
    | assign port_resp_bits_data = 32'h0;
    | assign port_resp_bits_rsp = 2'b00;
    |
    | // write
    | assign port_resp_valid = cur_state == WAIT;
    |endmodule
    """.stripMargin
    )
}

class TimingRamAXIIO extends Bundle{
    val clock = Input(Clock())
    val reset = Input(Bool())
    val port  = Flipped(new AXI4)
}

class TimingRamAXI extends Module with ErythrinaDefault{
    val io = IO(new TimingRamAXIIO)

    val convt = Module(new AXI42Ivy)
    val ram = Module(new TimingRam)

    convt.io.in     <> io.port
    convt.io.out    <> ram.io.port

    ram.io.clock    := io.clock
    ram.io.reset    := io.reset
}
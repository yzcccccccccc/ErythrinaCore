module DivDebug_top;

  wire  clock;
  wire  reset;
  wire  io_v;
  wire [31:0] io_a;
  wire [31:0] io_b;
  wire [1:0] io_op;
  wire [31:0] io_res;
  wire  io_res_valid;


DivDebug DivDebug (
    .clock(clock),
    .reset(reset),
    .io_v(io_v),
    .io_a(io_a),
    .io_b(io_b),
    .io_op(io_op),
    .io_res(io_res),
    .io_res_valid(io_res_valid)
  );
endmodule

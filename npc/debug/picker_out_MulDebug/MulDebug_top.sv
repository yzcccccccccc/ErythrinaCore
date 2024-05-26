module MulDebug_top;

  logic  clock;
  logic  reset;
  logic  io_v;
  logic [31:0] io_a;
  logic [31:0] io_b;
  logic [1:0] io_op;
  logic [31:0] io_res;


  MulDebug MulDebug (
    .clock(clock),
    .reset(reset),
    .io_v(io_v),
    .io_a(io_a),
    .io_b(io_b),
    .io_op(io_op),
    .io_res(io_res)
  );

  export "DPI-C" function get_clock;
  export "DPI-C" function set_clock;
  export "DPI-C" function get_reset;
  export "DPI-C" function set_reset;
  export "DPI-C" function get_io_v;
  export "DPI-C" function set_io_v;
  export "DPI-C" function get_io_a;
  export "DPI-C" function set_io_a;
  export "DPI-C" function get_io_b;
  export "DPI-C" function set_io_b;
  export "DPI-C" function get_io_op;
  export "DPI-C" function set_io_op;
  export "DPI-C" function get_io_res;
  export "DPI-C" function set_io_res;


  function void get_clock;
    output logic  value;
    value=clock;
  endfunction

  function void set_clock;
    input logic  value;
    clock=value;
  endfunction

  function void get_reset;
    output logic  value;
    value=reset;
  endfunction

  function void set_reset;
    input logic  value;
    reset=value;
  endfunction

  function void get_io_v;
    output logic  value;
    value=io_v;
  endfunction

  function void set_io_v;
    input logic  value;
    io_v=value;
  endfunction

  function void get_io_a;
    output logic [31:0] value;
    value=io_a;
  endfunction

  function void set_io_a;
    input logic [31:0] value;
    io_a=value;
  endfunction

  function void get_io_b;
    output logic [31:0] value;
    value=io_b;
  endfunction

  function void set_io_b;
    input logic [31:0] value;
    io_b=value;
  endfunction

  function void get_io_op;
    output logic [1:0] value;
    value=io_op;
  endfunction

  function void set_io_op;
    input logic [1:0] value;
    io_op=value;
  endfunction

  function void get_io_res;
    output logic [31:0] value;
    value=io_res;
  endfunction

  function void set_io_res;
    input logic [31:0] value;
    io_res=value;
  endfunction



initial begin
    $dumpfile("MulDebug.fst");
    $dumpvars(0, MulDebug_top);
 end 

endmodule

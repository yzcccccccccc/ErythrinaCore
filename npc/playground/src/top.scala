import chisel3._

class TOP extends Module {
    val bitWidth = 32
    val io = IO(new Bundle{
        val srcA    = Input(SInt(bitWidth.W))
        val srcB    = Input(SInt(bitWidth.W))
        val aluop   = Input(UInt(3.W))
        val res     = Output(UInt(bitWidth.W))
        val zero    = Output(Bool())
        val Overflow    = Output(Bool())
        val Carryout    = Output(Bool())
    })

    val myalu   = Module(new GenALU(bitWidth))
    
    myalu.io <> io
}
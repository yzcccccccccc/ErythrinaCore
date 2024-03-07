import chisel3._

class TOP extends Module {
    val io = IO(new Bundle{
        val inputs  = Input(UInt(8.W))
        val valid  = Output(Bool())
        val outputs = Output(UInt(3.W))
        val seg_res = Output(UInt(7.W))
    })

    val myencoder   = Module(new GenEncoder)
    
    myencoder.io <> io
}
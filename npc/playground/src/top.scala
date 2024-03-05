import chisel3._

class TOP extends Module {
    val io = IO(new Bundle{
        val Y   = Input(UInt(2.W))
        val X0  = Input(UInt(2.W))
        val X1  = Input(UInt(2.W))
        val X2  = Input(UInt(2.W))
        val X3  = Input(UInt(2.W))
        val F   = Output(UInt(2.W))
    })

    val myselector = Module(new GenSelector)

    myselector.io <> io
}
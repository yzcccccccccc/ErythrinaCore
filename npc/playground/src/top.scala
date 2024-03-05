import chisel3._

class TOP extends Module {
    val io = IO(new Bundle{
        val a   = Input(Bool())
        val b   = Input(Bool())
        val f   = Output(Bool())
    })

    io.f := io.a ^ io.b
}
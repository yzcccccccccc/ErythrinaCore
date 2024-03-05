import chisel3._
import chisel3.util._

class GenSelector extends Module {
    val io = IO(new Bundle{
        val Y   = Input(UInt(2.W))
        val X0  = Input(UInt(2.W))
        val X1  = Input(UInt(2.W))
        val X2  = Input(UInt(2.W))
        val X3  = Input(UInt(2.W))
        val F   = Output(UInt(2.W))
    })

    io.F := MuxLookup(io.Y, 0.U)(Seq(
        0.U -> io.X0,
        1.U -> io.X1,
        2.U -> io.X2,
        3.U -> io.X3
    ))
}
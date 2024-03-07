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

class GenEncoder extends Module {
    val io = IO(new Bundle{
        val inputs  = Input(UInt(8.W))
        val valid  = Output(Bool())
        val outputs = Output(UInt(3.W))
        val seg_res = Output(UInt(7.W))
    })

    val segmentTable = VecInit(
    "b1111110".asUInt(7.W), // 0
    "b0110000".asUInt(7.W), // 1
    "b1101101".asUInt(7.W), // 2
    "b1111001".asUInt(7.W), // 3
    "b0110011".asUInt(7.W), // 4
    "b1011011".asUInt(7.W), // 5
    "b1011111".asUInt(7.W), // 6
    "b1110000".asUInt(7.W), // 7
  )

    io.outputs := 7.U - PriorityEncoder(Reverse(io.inputs))
    io.valid := io.inputs.orR;
    io.seg_res := ~segmentTable(io.outputs)
}
import chisel3._
import chisel3.util._

class  GenALU(bitWidth: Int) extends Module {
    val io = IO(new Bundle{
        val srcA    = Input(SInt(bitWidth.W))
        val srcB    = Input(SInt(bitWidth.W))
        val aluop   = Input(UInt(3.W))
        val res     = Output(UInt(bitWidth.W))
        val zero    = Output(Bool())
        val Overflow    = Output(Bool())
        val Carryout    = Output(Bool())
    })

    val ALUop_list: List[(String, UInt)] = List(
        ("add", "b000".asUInt(3.W)),
        ("sub", "b001".asUInt(3.W)),
        ("not", "b010".asUInt(3.W)),
        ("and", "b011".asUInt(3.W)),
        ("or", "b100".asUInt(3.W)),
        ("xor", "b101".asUInt(3.W)),
        ("les", "b110".asUInt(3.W)),
        ("eq", "b111".asUInt(3.W))
    )

    val pairMap: Map[String, UInt] = ALUop_list.toMap

    val isadd = io.aluop === pairMap("add")
    val issub = io.aluop === pairMap("sub")
    val isnot = io.aluop === pairMap("not")
    val isand = io.aluop === pairMap("and")
    val isor = io.aluop === pairMap("or")
    val isxor = io.aluop === pairMap("xor")
    val isles = io.aluop === pairMap("les")
    val iseq = io.aluop === pairMap("eq")

    val need_minus = issub | isles | iseq
    val in_B = Wire(SInt(bitWidth.W))
    when (need_minus) {
        in_B := io.srcB
    }.otherwise {
        in_B := ~io.srcB
    }

    val add_sub_res = Wire(UInt(bitWidth.W))
    
    val tmp_res = Wire(UInt((bitWidth + 1).W))
    when(need_minus){
        tmp_res := (in_B +& io.srcA +& 1.S).asUInt
    }.otherwise{
        tmp_res := (io.srcA + in_B).asUInt
    }
    add_sub_res := tmp_res(bitWidth - 1, 0)

    io.Carryout := tmp_res(bitWidth)
    io.Overflow := (io.srcA(bitWidth - 1) & in_B(bitWidth - 1) & ~add_sub_res(bitWidth - 1)
                | ~io.srcA(bitWidth - 1) & ~in_B(bitWidth - 1) & add_sub_res(bitWidth - 1))

    when(isadd | issub) {
        io.res := add_sub_res
    }.elsewhen(isand){
        io.res := (io.srcA & io.srcB).asUInt
    }.elsewhen(isnot){
        io.res := (~io.srcA).asUInt
    }.elsewhen(isxor){
        io.res := (io.srcA ^ io.srcB).asUInt
    }.elsewhen(isor){
        io.res := (io.srcA | io.srcB).asUInt
    }.elsewhen(isles){
        io.res := io.Overflow ^ add_sub_res(bitWidth - 1)
    }.otherwise{
        io.res := (~add_sub_res).andR
    }

    io.zero := (~io.res).andR
}
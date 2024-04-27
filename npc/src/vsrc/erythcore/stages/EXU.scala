package erythcore

import chisel3._
import chisel3.util._

import utils._

class EXUIO extends Bundle with EXUtrait{
    val IDU2EXU     = Flipped(Decoupled(new ID2EXzip))
    val EXU2MEMU    = Decoupled(new EX2MEMzip)
    val EX2BPU      = Flipped(new EXU2BPUzip)
    val EX2CSR      = Flipped(new EXU2CSRzip)
}

class EXU extends Module with EXUtrait{
    val io = IO(new EXUIO)
    
    io.IDU2EXU.ready    := 1.B

    // ALU
    val ALU0 = Module(new ALU)
    val alu_res     = ALU0.io.ALUout.res
    val alu_zero    = ALU0.io.ALUout.zero
    ALU0.io.ALUin.src1  := io.IDU2EXU.bits.src1
    ALU0.io.ALUin.src2  := io.IDU2EXU.bits.src2
    ALU0.io.ALUin.aluop := io.IDU2EXU.bits.ALUop

    // to CSR
    val csrop = io.IDU2EXU.bits.CSRop
    io.EX2CSR.csrop := csrop
    io.EX2CSR.src1  := io.IDU2EXU.bits.src1
    io.EX2CSR.src2  := io.IDU2EXU.bits.src2
    val csr_res = io.EX2CSR.rdata

    // TODO: give the result to BPU
    // to BPU
    io.EX2BPU.aluout <> ALU0.io.ALUout

    // to IDU
    //io.IDU2EXU.ready        := io.IDU2EXU.valid & io.EXU2MEMU.ready

    // to MEM!
    io.EXU2MEMU.valid       := io.IDU2EXU.valid
    io.EXU2MEMU.bits.inst   := io.IDU2EXU.bits.inst
    io.EXU2MEMU.bits.pc     := io.IDU2EXU.bits.pc
    io.EXU2MEMU.bits.LSUop  := io.IDU2EXU.bits.LSUop
    io.EXU2MEMU.bits.addr   := Mux(csrop === CSRop.nop, alu_res, csr_res)
    io.EXU2MEMU.bits.rd     := io.IDU2EXU.bits.rd
    io.EXU2MEMU.bits.rf_wen := io.IDU2EXU.bits.rf_wen
    io.EXU2MEMU.bits.data2store := io.IDU2EXU.bits.data2store

}
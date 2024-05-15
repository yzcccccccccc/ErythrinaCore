package erythcore

import chisel3._
import chisel3.util._

import utils._

class EXUIO extends Bundle with EXUtrait{
    val idu_to_exu     = Flipped(Decoupled(new id_to_ex_zip))
    val exu_to_memu    = Decoupled(new ex_to_mem_zip)
    val EX2BPU      = Flipped(new EXU2BPUzip)
    val EX2CSR      = Flipped(new EXU2CSRzip)
}

class EXU extends Module with EXUtrait{
    val io = IO(new EXUIO)
    
    io.idu_to_exu.ready    := 1.B

    // ALU
    val ALU0 = Module(new ALU)
    val alu_res     = ALU0.io.ALUout.res
    val alu_zero    = ALU0.io.ALUout.zero
    ALU0.io.ALUin.src1  := io.idu_to_exu.bits.src1
    ALU0.io.ALUin.src2  := io.idu_to_exu.bits.src2
    ALU0.io.ALUin.aluop := io.idu_to_exu.bits.ALUop

    // to CSR
    val csrop = io.idu_to_exu.bits.CSRop
    io.EX2CSR.csrop := csrop
    io.EX2CSR.src1  := io.idu_to_exu.bits.src1
    io.EX2CSR.src2  := io.idu_to_exu.bits.src2
    val csr_res = io.EX2CSR.rdata

    // TODO: give the result to BPU
    // to BPU
    io.EX2BPU.aluout <> ALU0.io.ALUout

    // to IDU
    //io.idu_to_exu.ready        := io.idu_to_exu.valid & io.EXU2MEMU.ready

    // to MEM!
    io.exu_to_memu.valid       := io.idu_to_exu.valid
    io.exu_to_memu.bits.inst   := io.idu_to_exu.bits.inst
    io.exu_to_memu.bits.pc     := io.idu_to_exu.bits.pc
    io.exu_to_memu.bits.LSUop  := io.idu_to_exu.bits.LSUop
    io.exu_to_memu.bits.addr   := Mux(csrop === CSRop.nop, alu_res, csr_res)
    io.exu_to_memu.bits.rd     := io.idu_to_exu.bits.rd
    io.exu_to_memu.bits.rf_wen := io.idu_to_exu.bits.rf_wen
    io.exu_to_memu.bits.data2store := io.idu_to_exu.bits.data2store

}
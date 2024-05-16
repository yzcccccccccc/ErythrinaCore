package erythcore

import chisel3._
import chisel3.util._

import utils._

class EXUIO extends Bundle with EXUtrait{
    val idu_exu_zip     = Flipped(Decoupled(new ID_EX_zip))
    val exu_memu_zip    = Decoupled(new EX_MEM_zip)
    val exu_bpu_zip     = Flipped(new EXU_BPU_zip)
    val exu_csr_zip     = Flipped(new EXU_CSR_zip)
    val exu_fwd_zip     = Flipped(new FWD_RESP_zip)
}

class EXU extends Module with EXUtrait{
    val io = IO(new EXUIO)
    
    io.idu_exu_zip.ready    := 1.B
    val content_valid   = io.idu_exu_zip.bits.content_valid

    // ALU
    val ALU0 = Module(new ALU)
    val alu_res     = ALU0.io.ALUout.res
    val alu_zero    = ALU0.io.ALUout.zero
    ALU0.io.ALUin.src1  := io.idu_exu_zip.bits.src1
    ALU0.io.ALUin.src2  := io.idu_exu_zip.bits.src2
    ALU0.io.ALUin.aluop := io.idu_exu_zip.bits.ALUop

    // to CSR
    val csrop = io.idu_exu_zip.bits.CSRop
    io.exu_csr_zip.csrop := csrop
    io.exu_csr_zip.src1  := io.idu_exu_zip.bits.src1
    io.exu_csr_zip.src2  := io.idu_exu_zip.bits.src2
    val csr_res = io.exu_csr_zip.rdata

    // to FWD
    io.exu_fwd_zip.datasrc  := Mux(LSUop.isLoad(io.idu_exu_zip.bits.LSUop), FwdDataSrc.FROM_MEM, Mux(csrop === CSRop.nop, FwdDataSrc.FROM_ALU, FwdDataSrc.FROM_CSR))
    io.exu_fwd_zip.rd       := io.idu_exu_zip.bits.rd
    io.exu_fwd_zip.wdata    := Mux(csrop === CSRop.nop, alu_res, csr_res)
    io.exu_fwd_zip.wen      := io.idu_exu_zip.bits.rf_wen
    io.exu_fwd_zip.valid    := 1.B

    // to BPU
    io.exu_bpu_zip.aluout <> ALU0.io.ALUout

    // to IDU
    io.idu_exu_zip.ready        := io.exu_memu_zip.ready

    // to MEM!
    io.exu_memu_zip.valid       := 1.B
    io.exu_memu_zip.bits.content_valid   := content_valid
    io.exu_memu_zip.bits.inst            := io.idu_exu_zip.bits.inst
    io.exu_memu_zip.bits.pc              := io.idu_exu_zip.bits.pc
    io.exu_memu_zip.bits.LSUop           := io.idu_exu_zip.bits.LSUop
    io.exu_memu_zip.bits.addr_or_res     := Mux(csrop === CSRop.nop, alu_res, csr_res)
    io.exu_memu_zip.bits.rd              := io.idu_exu_zip.bits.rd
    io.exu_memu_zip.bits.rf_wen          := io.idu_exu_zip.bits.rf_wen
    io.exu_memu_zip.bits.data2store      := io.idu_exu_zip.bits.data2store
    io.exu_memu_zip.bits.exception.isEbreak  := content_valid & io.exu_csr_zip.isEBREAK
    io.exu_memu_zip.bits.exception.isUnknown := io.idu_exu_zip.bits.exception.isUnknown

}
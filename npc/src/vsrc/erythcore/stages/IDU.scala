package erythcore

import chisel3._
import chisel3.util._

import utils._

// IDU!
class IDUIO extends Bundle with IDUtrait{
    val ifu_idu_zip = Flipped(Decoupled(new IF_ID_zip))
    val idu_exu_zip = Decoupled(new ID_EX_zip)

    val idu_bpu_zip     = Flipped(new IDU_BPU_zip)      // to BPU
    val idu_bpu_trigger = Output(Bool())
    val rf_rd_port      = Flipped(new RegFileIN)        // to Regfile
    val bpu_redirect    = Flipped(new RedirectInfo)
    val idu_fwd_zip     = Flipped(new FWD_REQ_zip)

    // perf
    val idu_perf_probe = Flipped(new PerfIDU)
}

class IDU extends Module with IDUtrait{
    val io = IO(new IDUIO)
    
    val content_valid   = io.ifu_idu_zip.bits.content_valid & ~io.bpu_redirect.redirect
    val instr           = io.ifu_idu_zip.bits.inst
    val pc              = io.ifu_idu_zip.bits.pc

    // Decode Instr
    val decodeList = ListLookup(instr, Instructions.decodeDefault, Instructions.decode_table)
    val instType :: aluop_raw :: lsuop_raw :: bpuop_raw :: csrop_raw :: Nil = decodeList
    val rs2 = instr(24, 20)
    val rs1 = instr(19, 15)
    val rd  = instr(11, 7)

    // real op
    val bpuop = Mux(content_valid, bpuop_raw, BPUop.nop)
    val aluop = Mux(content_valid, aluop_raw, ALUop.nop)
    val lsuop = Mux(content_valid, lsuop_raw, LSUop.nop)
    val csrop = Mux(content_valid, csrop_raw, CSRop.nop)

    // RegFile
    io.rf_rd_port.raddr1 := rs1
    io.rf_rd_port.raddr2 := rs2

    // Get src
    val immj = Mux(instr(3, 3) === 1.B, SignExt(Cat(instr(31), instr(19, 12), instr(20), instr(30, 21), 0.U(1.W)), XLEN), SignExt(instr(31, 20), XLEN))
    val imm = LookupTree(instType, List(
        TypeN   -> SignExt(instr(31, 20), XLEN),
        TypeI   -> SignExt(instr(31, 20), XLEN),
        TypeS   -> SignExt(Cat(instr(31, 25), instr(11, 7)), XLEN),
        TypeB   -> SignExt(Cat(instr(31), instr(7), instr(30, 25), instr(11, 8), 0.U(1.W)), XLEN),
        TypeU   -> SignExt(Cat(instr(31, 12), 0.U(12.W)), XLEN),
        TypeJ   -> immj
    ))

    // Generate Decode Information
    // TODO: What About TypeN ??
    val srcTypeList = List(             // type -> (src1_type, src2_type)
        TypeI   -> (SrcType.reg, SrcType.imm),
        TypeB   -> (SrcType.reg, SrcType.reg),
        TypeJ   -> (SrcType.pc, SrcType.const),
        TypeR   -> (SrcType.reg, SrcType.reg),
        TypeS   -> (SrcType.reg, SrcType.imm),
        TypeU   -> (SrcType.imm, SrcType.pc)
    )

    val src1_type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._1)))
    val src2_type = LookupTree(instType, srcTypeList.map(p => (p._1, p._2._2)))

    // forward
    io.idu_fwd_zip.rs1 := rs1
    io.idu_fwd_zip.rs1_en := src1_type === SrcType.reg && rs1 =/= 0.U | bpuop === BPUop.jalr | ~CSRop.usei(csrop) & ~instType === TypeN
    io.idu_fwd_zip.rs2 := rs2
    io.idu_fwd_zip.rs2_en := src2_type === SrcType.reg && rs2 =/= 0.U | LSUop.isStore(lsuop)
    val fwd_rs1_occ = io.idu_fwd_zip.rs1_occ
    val fwd_rs2_occ = io.idu_fwd_zip.rs2_occ
    val fwd_rdata1  = io.idu_fwd_zip.rdata1
    val fwd_rdata2  = io.idu_fwd_zip.rdata2
    val fwd_pause   = io.idu_fwd_zip.pause

    val rdata1 = Mux(fwd_rs1_occ, fwd_rdata1, io.rf_rd_port.rdata1)
    val rdata2 = Mux(fwd_rs2_occ, fwd_rdata2, io.rf_rd_port.rdata2)

    val src1 = LookupTree(src1_type, List(
        SrcType.imm     -> imm,
        SrcType.pc      -> pc,
        SrcType.reg     -> rdata1
    ))
    val src2 = LookupTree(src2_type, List(
        SrcType.imm     -> imm,
        SrcType.pc      -> pc,
        SrcType.reg     -> rdata2,
        SrcType.const   -> 4.U
    ))

    val rf_wen = ~(instType === TypeB || instType === TypeS || instType === TypeN)

    // CSR srcs
    val usei = CSRop.usei(csrop)
    val csr_typI = Mux(usei, ZeroExt(rs1, XLEN), rdata1)
    val csr_src1 = Mux(instType === TypeN, pc, csr_typI) // TypeN and TypeI
    val csr_src2 = imm     

    // to BPU
    io.idu_bpu_zip.content_valid := io.ifu_idu_zip.bits.content_valid
    io.idu_bpu_zip.bpuop := bpuop
    io.idu_bpu_zip.src1  := Mux(bpuop === BPUop.jalr, rdata1, pc)
    io.idu_bpu_zip.src2  := imm
    io.idu_bpu_zip.pc    := pc
    io.idu_bpu_trigger   := io.idu_exu_zip.fire

    // to IFU!
    io. ifu_idu_zip.ready            :=  io.idu_exu_zip.ready & io.idu_exu_zip.valid

    // to EXU!
    io.idu_exu_zip.valid                := ~fwd_pause | ~content_valid
    io.idu_exu_zip.bits.content_valid   := content_valid
    io.idu_exu_zip.bits.pc              := pc
    io.idu_exu_zip.bits.inst            := instr
    
    io.idu_exu_zip.bits.src1        := Mux(csrop === CSRop.nop, src1, csr_src1)
    io.idu_exu_zip.bits.src2        := Mux(csrop === CSRop.nop, src2, csr_src2)
    io.idu_exu_zip.bits.ALUop       := aluop
    io.idu_exu_zip.bits.BPUop       := bpuop
    io.idu_exu_zip.bits.LSUop       := lsuop
    io.idu_exu_zip.bits.CSRop       := csrop
    io.idu_exu_zip.bits.data2store  := rdata2
    io.idu_exu_zip.bits.rd          := rd
    io.idu_exu_zip.bits.rf_wen      := rf_wen & content_valid
    io.idu_exu_zip.bits.exception.isEbreak   := 0.B
    io.idu_exu_zip.bits.exception.isUnknown  := instType === TypeER & content_valid

    // Perf
    io.idu_perf_probe.cal_inst_event := content_valid & aluop =/= ALUop.nop 
    io.idu_perf_probe.csr_inst_event := content_valid & csrop =/= CSRop.nop 
    io.idu_perf_probe.ld_inst_event := content_valid & (lsuop === LSUop.lw || lsuop === LSUop.lh || lsuop === LSUop.lhu || lsuop === LSUop.lb || lsuop === LSUop.lbu) 
    io.idu_perf_probe.st_inst_event := content_valid & (lsuop === LSUop.sw || lsuop === LSUop.sh || lsuop === LSUop.sb) 
    io.idu_perf_probe.j_inst_event := content_valid & (bpuop === BPUop.jal || bpuop === BPUop.jalr) 
    io.idu_perf_probe.b_inst_event := content_valid & (bpuop === BPUop.beq || bpuop === BPUop.bne || bpuop === BPUop.blt || bpuop === BPUop.bge || bpuop === BPUop.bltu || bpuop === BPUop.bgeu) 
}
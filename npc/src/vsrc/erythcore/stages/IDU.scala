package erythcore

import chisel3._
import chisel3.util._

import utils._

// IDU!
class IDUIO extends Bundle with IDUtrait{
    val step    = Input(Bool())
    val ifu_to_idu = Flipped(Decoupled(new if_to_id_zip))
    val idu_to_exu = Decoupled(new id_to_ex_zip)
    val ID2BPU = Flipped(new IDU2BPUzip)           // 2 BPU
    val RFRead  = Flipped(new RegFileIN)        // 2 Regfile
    val BPU2IDU = Flipped(new RedirectInfo)

    // perf
    val idu_perf_probe = Flipped(new PerfIDU)
}

class IDU extends Module with IDUtrait{
    val io = IO(new IDUIO)

    io.ifu_to_idu.ready    := 1.B
    
    val instr   = io.ifu_to_idu.bits.inst
    val pc      = io.ifu_to_idu.bits.pc

    // Decode Instr
    val decodeList = ListLookup(instr, Instructions.decodeDefault, Instructions.decode_table)
    val instType :: aluop :: lsuop :: bpuop :: csrop :: Nil = decodeList
    val rs2 = instr(24, 20)
    val rs1 = instr(19, 15)
    val rd  = instr(11, 7)

    // RegFile
    io.RFRead.raddr1 := rs1
    io.RFRead.raddr2 := rs2

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
    val rdata1 = io.RFRead.rdata1
    val rdata2 = io.RFRead.rdata2

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
    io.ID2BPU.bpuop := bpuop
    io.ID2BPU.src1  := Mux(bpuop === BPUop.jalr, rdata1, pc)
    io.ID2BPU.src2  := imm
    io.ID2BPU.pc    := pc

    // unknown inst
    if (!ErythrinaSetting.isSTA){
        val HaltUnkonwInst = Module(new haltUnknownInst)
        HaltUnkonwInst.io.halt_trigger := instType === TypeER & io.ifu_to_idu.valid
    }

    // to IFU!
    //io.ifu_to_idu.ready            := io.idu_to_exu.valid & io.idu_to_exu.ready

    // to EXU!
    io.idu_to_exu.valid            := io.ifu_to_idu.valid
    io.idu_to_exu.bits.src1        := Mux(csrop === CSRop.nop, src1, csr_src1)
    io.idu_to_exu.bits.src2        := Mux(csrop === CSRop.nop, src2, csr_src2)
    io.idu_to_exu.bits.ALUop       := aluop
    io.idu_to_exu.bits.BPUop       := bpuop
    io.idu_to_exu.bits.LSUop       := lsuop
    io.idu_to_exu.bits.CSRop       := csrop
    io.idu_to_exu.bits.data2store  := rdata2
    io.idu_to_exu.bits.rd          := rd
    io.idu_to_exu.bits.rf_wen      := rf_wen & io.ifu_to_idu.valid
    io.idu_to_exu.bits.pc          := pc
    io.idu_to_exu.bits.inst        := instr

    // Perf
    io.idu_perf_probe.cal_inst_event := io.ifu_to_idu.valid & aluop =/= ALUop.nop & io.step
    io.idu_perf_probe.csr_inst_event := io.ifu_to_idu.valid & csrop =/= CSRop.nop & io.step
    io.idu_perf_probe.ld_inst_event := io.ifu_to_idu.valid & (lsuop === LSUop.lw || lsuop === LSUop.lh || lsuop === LSUop.lhu || lsuop === LSUop.lb || lsuop === LSUop.lbu) & io.step
    io.idu_perf_probe.st_inst_event := io.ifu_to_idu.valid & (lsuop === LSUop.sw || lsuop === LSUop.sh || lsuop === LSUop.sb) & io.step
    io.idu_perf_probe.j_inst_event := io.ifu_to_idu.valid & (bpuop === BPUop.jal || bpuop === BPUop.jalr) & io.step
    io.idu_perf_probe.b_inst_event := io.ifu_to_idu.valid & (bpuop === BPUop.beq || bpuop === BPUop.bne || bpuop === BPUop.blt || bpuop === BPUop.bge || bpuop === BPUop.bltu || bpuop === BPUop.bgeu) & io.step
}
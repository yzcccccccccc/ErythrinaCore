package erythcore

import chisel3._
import chisel3.util._

import utils._
import chisel3.util.circt.Mux2Cell

object CSRop {
    def nop     = "b1000".U
    def jmp     = "b0000".U
    def wrt     = "b0001".U         // write
    def set     = "b0010".U         // set
    def clr     = "b0011".U
    def wrti    = "b0101".U
    def seti    = "b0110".U
    def clri    = "b0111".U

    def usei(csrop: UInt) = csrop(2)
    def iswrt(csrop: UInt) = ~csrop(1) & csrop(0)
    def isset(csrop: UInt) = csrop(1) & ~csrop(0)
    def isclr(csrop: UInt) = csrop(1) & csrop(1)
}

trait CSRtrait extends ErythrinaDefault{
    val CSRopLEN = 4
}

class EXU2CSRzip extends Bundle with CSRtrait{
    val src1    = Input(UInt(XLEN.W))
    val src2    = Input(UInt(XLEN.W))
    val csrop   = Input(UInt(CSRopLEN.W))
    val rdata   = Output(UInt(XLEN.W))
}

class WBU2CSRzip extends Bundle with CSRtrait{
    // TODO: TBD, in the future (pipeline?), CSR write action will be triggered in WB
}

class CSR2BPUzip extends  Bundle with CSRtrait{
    val target_pc = Output(UInt(XLEN.W))
}

class CSRIO extends Bundle with CSRtrait{
    val EXU2CSR = new EXU2CSRzip
    val CSR2BPU = new CSR2BPUzip
    val en = Input(Bool())
}

object TrapCause{
    def UECALL  = 8.U
    def SECALL  = 9.U
    def MECALL  = 11.U
}

object CSRnum{
    def mstatus     = 0x300.U
    def mtvec       = 0x305.U
    def mepc        = 0x341.U
    def mcause      = 0x342.U
    def mvendorid   = 0xf11.U
    def marchid     = 0xf12.U
}

class CSR extends Module with ErythrinaDefault{
    val io = IO(new CSRIO)

    val src1    = io.EXU2CSR.src1
    val src2    = io.EXU2CSR.src2
    val csrop   = io.EXU2CSR.csrop

    // priv
    def privECALL   = 0x000.U
    def privEBREAK  = 0x001.U
    def privMRET    = 0x302.U

    // Machine
    val mstatus     = RegInit(UInt(XLEN.W), 0x1800.U)
    val mcause      = RegInit(UInt(XLEN.W), 0.U)
    val mepc        = RegInit(UInt(XLEN.W), 0.U)
    val mtvec       = RegInit(UInt(XLEN.W), 0.U)
    val mvendorid   = RegInit(UInt(XLEN.W), 0x79737978.U)
    val marchid     = RegInit(UInt(XLEN.W), 0x1d4b42.U)

    val csrnum      = src2(11, 0)
    val isECALL     = csrop === CSRop.jmp && csrnum === privECALL
    val isEBREAK    = csrop === CSRop.jmp && csrnum === privEBREAK
    val isMRET      = csrop === CSRop.jmp && csrnum === privMRET

    // to BPU
    val tar_pc      = Mux1H(Seq(
        isECALL     -> mtvec,
        isEBREAK    -> 0.U,
        isMRET      -> mepc
    ))
    io.CSR2BPU.target_pc := tar_pc

    // choose the reg!
    val csrval  = LookupTreeDefault(csrnum, 0.U, List(
        CSRnum.mcause       -> mcause,
        CSRnum.mepc         -> mepc,
        CSRnum.mstatus      -> mstatus,
        CSRnum.mtvec        -> mtvec,
        CSRnum.marchid      -> marchid,
        CSRnum.mvendorid    -> mvendorid
    ))
    io.EXU2CSR.rdata    := csrval
    
    // update
    val csr_wen = (csrop =/= CSRop.nop) && (csrop =/= CSRop.jmp)
    val isWRT   = CSRop.iswrt(csrop)
    val isSET   = CSRop.isset(csrop)
    val isCLR   = CSRop.isclr(csrop)
    val csr_new = Mux1H(Seq(
        isWRT   -> src1,
        isSET   -> (csrval | src1),
        isCLR   -> (csrval & ~src1)
    ))
    when (io.en & csr_wen){
        switch (csrnum){
            is (CSRnum.mcause){
                mcause  := csr_new
            }
            is (CSRnum.mepc){
                mepc    := csr_new
            }
            is (CSRnum.mstatus){
                mstatus := csr_new
            }
            is (CSRnum.mtvec){
                mtvec   := csr_new
            }
        }
    }

    // halt if is EBREAK
    if (!ErythrinaSetting.isSTA){
        val HaltEbreak = Module(new haltEbreak)
        HaltEbreak.io.halt_trigger    := isEBREAK
    }

    // ecall
    when (isECALL & io.en){ // update in WB?
        mepc    := src1
        mcause  := TrapCause.MECALL
    }
}
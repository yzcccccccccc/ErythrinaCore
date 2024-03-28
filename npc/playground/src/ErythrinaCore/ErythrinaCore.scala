package ErythrinaCore

import chisel3._
import chisel3.util._

import bus.mem._

class ErythrinaCoreIO extends Bundle with ErythrinaDefault{
    val MemReq1      = Decoupled(new MemReqIO)
    val MemResp1     = Flipped(Decoupled(new MemRespIO))
    val MemReq2      = Decoupled(new MemReqIO)
    val MemResp2     = Flipped(Decoupled(new MemRespIO))
    val InstCommit  = new ErythrinaCommit
}

// to be continued. this file is for aggregating the core parts
class ErythrinaCore extends Module with ErythrinaDefault{
    val io = IO(new ErythrinaCoreIO)
    val IFU_inst    = Module(new IFU)
    val IDU_inst    = Module(new IDU)
    val EXU_inst    = Module(new EXU)
    val MEMU_inst   = Module(new MEMU)
    val WBU_inst    = Module(new WBU)

    // FSM
    val sIF :: sIF_Recv :: sID :: sEX :: sMEM :: sMEM_Recv :: sWB :: Nil = Enum(7)
    val state   = RegInit(sIF)
    switch (state){
        is (sIF){
            when (IFU_inst.io.IFU_memReq.fire){
                state := sIF_Recv
            }
        }
        is (sIF_Recv){
            when (IFU_inst.io.IFU_memResp.fire){
                state := sID
            }
        }
        is (sID){
            state := sEX
        }
        is (sEX){
            state := sMEM
        }
        is (sMEM){
            when (MEMU_inst.io.MEMU_memReq.fire){
                state := sMEM_Recv
            }.elsewhen(MEMU_inst.io.MEMU_memReq.valid){
                state := sMEM
            }.otherwise{
                state := sWB
            }
        }
        is (sMEM_Recv){
            when (MEMU_inst.io.MEMU_memResp.fire){
                state := sWB
            }
        }
        is (sWB){
            state := sIF
        }
    }
    IFU_inst.io.step    := state === sWB
    WBU_inst.io.step    := state === sWB
    MEMU_inst.io.en     := state === sMEM
    
    // regfile
    val regfile     = Module(new RegFile)
    regfile.io.readIO <> IDU_inst.io.RFRead
    regfile.io.writeIO <> WBU_inst.io.RegWriteIO

    // BPU
    val BPU_inst    = Module(new BPU)
    IFU_inst.io.BPU2IFU <> BPU_inst.io.IF_Redirect
    IDU_inst.io.BPU2IDU <> BPU_inst.io.ID_Redirect
    IDU_inst.io.ID2BPU  <> BPU_inst.io.ID2BPU
    EXU_inst.io.EX2BPU  <> BPU_inst.io.EX2BPU

    // pipelines
    IFU_inst.io.IFU2IDU <> IDU_inst.io.IFU2IDU
    IDU_inst.io.IDU2EXU <> EXU_inst.io.IDU2EXU
    EXU_inst.io.EXU2MEMU <> MEMU_inst.io.EXU2MEMU
    MEMU_inst.io.MEMU2WBU <> WBU_inst.io.MEMU2WBU

    // mem
    val MM_inst     = Module(new MemManager2x2)
    MM_inst.io.MemReq1 <> io.MemReq1
    MM_inst.io.MemResp1 <> io.MemResp1
    MM_inst.io.MemReq2  <> io.MemReq2
    MM_inst.io.MemResp2 <> io.MemResp2
    MM_inst.io.IFU_Req <> IFU_inst.io.IFU_memReq
    MM_inst.io.IFU_Resp <> IFU_inst.io.IFU_memResp
    MM_inst.io.MEMU_Req <> MEMU_inst.io.MEMU_memReq
    MM_inst.io.MEMU_Resp <> MEMU_inst.io.MEMU_memResp

    // commit
    io.InstCommit <> WBU_inst.io.inst_commit
}
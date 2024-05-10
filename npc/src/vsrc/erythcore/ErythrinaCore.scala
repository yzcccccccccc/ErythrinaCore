package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import bus.ivybus.IvyBus
import bus.axi4._

class ErythrinaCoreIO extends Bundle with ErythrinaDefault{
    val mem_port1    = new AXI4
    val mem_port2    = new AXI4
    val InstCommit   = new ErythrinaCommit
}

// to be continued. this file is for aggregating the core parts
class ErythrinaCore extends Module with ErythrinaDefault{
    val io = IO(new ErythrinaCoreIO)
    val IFU_inst    = Module(new IFU)
    val IDU_inst    = Module(new IDU)
    val EXU_inst    = Module(new EXU)
    val MEMU_inst   = Module(new MEMU)
    val WBU_inst    = Module(new WBU)

    val BPU_inst    = Module(new BPU)
    val CSR_inst    = Module(new CSR)
    val regfile     = Module(new RegFile)

    // FSM
    val sIF :: sIF_Recv :: sID :: sEX :: sMEM :: sMEM_Recv :: sWB :: Nil = Enum(7)
    val state   = RegInit(sIF)
    switch (state){
        is (sIF){
            when (IFU_inst.io.ifu_mem.req.fire){
                state := sIF_Recv
            }
        }
        is (sIF_Recv){
            when (IFU_inst.io.ifu_mem.resp.fire){
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
            when (MEMU_inst.io.memu_mem.req.fire){
                state := sMEM_Recv
            }.elsewhen(MEMU_inst.io.memu_mem.req.valid){
                state := sMEM
            }.otherwise{
                state := sWB
            }
        }
        is (sMEM_Recv){
            when (MEMU_inst.io.memu_mem.resp.fire){
                state := sWB
            }
        }
        is (sWB){
            state := sIF
        }
    }
    IFU_inst.io.req_en  := state === sIF
    IFU_inst.io.step    := state === sWB
    WBU_inst.io.step    := state === sWB
    MEMU_inst.io.en     := state === sMEM
    CSR_inst.io.en      := state === sWB

    // CSR
    CSR_inst.io.CSR2BPU <> BPU_inst.io.CSR2BPU
    CSR_inst.io.EXU2CSR <> EXU_inst.io.EX2CSR
    
    // regfile 
    regfile.io.readIO <> IDU_inst.io.RFRead
    regfile.io.writeIO <> WBU_inst.io.RegWriteIO

    // BPU  
    IFU_inst.io.BPU2IFU <> BPU_inst.io.IF_Redirect
    IDU_inst.io.BPU2IDU <> BPU_inst.io.ID_Redirect
    IDU_inst.io.ID2BPU  <> BPU_inst.io.ID2BPU
    EXU_inst.io.EX2BPU  <> BPU_inst.io.EX2BPU

    // TODO: pipelines
    IFU_inst.io.IFU2IDU <> IDU_inst.io.IFU2IDU
    IDU_inst.io.IDU2EXU <> EXU_inst.io.IDU2EXU
    EXU_inst.io.EXU2MEMU <> MEMU_inst.io.EXU2MEMU
    MEMU_inst.io.MEMU2WBU <> WBU_inst.io.MEMU2WBU

    // TODO: mem (change to LSU)
    val MM_inst     = Module(new MemManager2x2)
    MM_inst.io.mem1     <> io.mem_port1
    MM_inst.io.mem2     <> io.mem_port2
    MM_inst.io.ifu_in   <> IFU_inst.io.ifu_mem
    MM_inst.io.memu_in  <> MEMU_inst.io.memu_mem
    
    // commit
    io.InstCommit <> WBU_inst.io.inst_commit
}
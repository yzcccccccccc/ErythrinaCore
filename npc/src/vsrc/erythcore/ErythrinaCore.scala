package erythcore

import chisel3._
import chisel3.util._

import bus.mem._
import bus.ivybus._
import bus.axi4._

import utils._

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
    IDU_inst.io.step    := state === sWB
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
    IFU_inst.io.ifu_to_idu <> IDU_inst.io.ifu_to_idu
    IDU_inst.io.idu_to_exu <> EXU_inst.io.idu_to_exu
    EXU_inst.io.exu_to_memu <> MEMU_inst.io.exu_to_memu
    MEMU_inst.io.memu_to_wbu <> WBU_inst.io.memu_to_wbu

    // TODO: mem (change to LSU)
    val ifu_conv    = Module(new Ivy2AXI4)
    val memu_conv   = Module(new Ivy2AXI4)

    ifu_conv.io.in  <> IFU_inst.io.ifu_mem
    ifu_conv.io.out <> io.mem_port1

    memu_conv.io.in  <> MEMU_inst.io.memu_mem
    memu_conv.io.out <> io.mem_port2
    
    // commit
    io.InstCommit <> WBU_inst.io.inst_commit

    // Performance Counter
    val perfbox    = Module(new PerfBox)
    perfbox.io.inst_trigger := io.InstCommit.valid
    perfbox.io.ifu_perf_probe <> IFU_inst.io.ifu_perf_probe
    perfbox.io.idu_perf_probe <> IDU_inst.io.idu_perf_probe
    perfbox.io.memu_perf_probe <> MEMU_inst.io.memu_perf_probe
}
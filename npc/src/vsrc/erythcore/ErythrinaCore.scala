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
    val FWD_inst    = Module(new FWD)
    val regfile     = Module(new RegFile)

    // CSR
    CSR_inst.io.csr_bpu_zip <> BPU_inst.io.csr_bpu_zip
    CSR_inst.io.exu_csr_zip <> EXU_inst.io.exu_csr_zip
    
    // regfile 
    regfile.io.rf_rport <> IDU_inst.io.rf_rd_port
    regfile.io.rf_wport <> WBU_inst.io.RegWriteIO

    // BPU  
    IFU_inst.io.bpu_redirect <> BPU_inst.io.IF_Redirect
    IDU_inst.io.bpu_redirect <> BPU_inst.io.ID_Redirect
    IFU_inst.io.ifu_bpu_zip  <> BPU_inst.io.ifu_bpu_zip
    IDU_inst.io.idu_bpu_zip  <> BPU_inst.io.idu_bpu_zip
    EXU_inst.io.exu_bpu_zip  <> BPU_inst.io.exu_bpu_zip
    BPU_inst.io.idu_bpu_trigger := IDU_inst.io.idu_bpu_trigger
    BPU_inst.io.exu_bpu_trigger := EXU_inst.io.exu_bpu_trigger

    // FWD
    IDU_inst.io.idu_fwd_zip     <> FWD_inst.io.req
    EXU_inst.io.exu_fwd_zip     <> FWD_inst.io.resp(0)
    MEMU_inst.io.memu_fwd_zip   <> FWD_inst.io.resp(1)
    WBU_inst.io.wbu_fwd_zip     <> FWD_inst.io.resp(2)


    // TODO: pipelines
    StageConnect(IFU_inst.io. ifu_idu_zip, IDU_inst.io. ifu_idu_zip)
    StageConnect(IDU_inst.io.idu_exu_zip, EXU_inst.io.idu_exu_zip)
    StageConnect(EXU_inst.io.exu_memu_zip, MEMU_inst.io.exu_memu_zip)
    StageConnect(MEMU_inst.io.memu_wbu_zip, WBU_inst.io.memu_wbu_zip)

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
    perfbox.io.ifu_perf_probe   <> IFU_inst.io.ifu_perf_probe
    perfbox.io.idu_perf_probe   <> IDU_inst.io.idu_perf_probe
    perfbox.io.memu_perf_probe  <> MEMU_inst.io.memu_perf_probe
    perfbox.io.bpu_perf_probe   <> BPU_inst.io.bpu_perf_probe
}
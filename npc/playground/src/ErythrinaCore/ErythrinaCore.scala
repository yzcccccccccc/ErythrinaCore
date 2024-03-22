package ErythrinaCore

import chisel3._
import chisel3.util._

import bus.mem._

class ErythrinaCoreIO extends Bundle with ErythrinaDefault{
    val MemReq      = Decoupled(new MemReqIO)
    val MemResp     = Flipped(Decoupled(new MemRespIO))
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

    // pipelines
    IFU_inst.io.IFU2IDU <> IDU_inst.io.IFU2IDU
    IDU_inst.io.IDU2EXU <> EXU_inst.io.IDU2EXU
    EXU_inst.io.EXU2MEMU <> MEMU_inst.io.EXU2MEMU
    MEMU_inst.io.MEMU2WBU <> WBU_inst.io.MEMU2WBU

    // mem
    val MM_inst     = Module(new MemManager)
    MM_inst.io.MemReq <> io.MemReq
    MM_inst.io.MemResp <> io.MemResp
    MM_inst.io.IFU_Req <> IFU_inst.io.IFU_memReq
    MM_inst.io.IFU_Resp <> IFU_inst.io.IFU_memResp
    MM_inst.io.MEMU_Req <> MEMU_inst.io.MEMU_memReq
    MM_inst.io.MEMU_Resp <> MEMU_inst.io.MEMU_memResp

    // commit
    io.InstCommit <> WBU_inst.io.inst_commit
}
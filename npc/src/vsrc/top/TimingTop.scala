package top

import chisel3._
import bus.mem._
import erythcore._
import bus.axi4._
import device._

// this module is mainly for analyzing the timing performance of the core
class TimingTop extends Module with ErythrinaDefault{
    val erythrinacore = Module(new ErythrinaCore(isSTA = true))

    val axi4clint   = Module(new AXI4CLINT)

    if (MARCH == "H"){
        val InstMem = Module(new SimpleRamAXI)
        val DataMem = Module(new SimpleRamAXI)

        InstMem.io.clock := clock
        InstMem.io.reset := reset
        DelayConnect(erythrinacore.io.mem_port1, InstMem.io.port)

        DataMem.io.clock := clock
        DataMem.io.reset := reset
        DelayConnect(erythrinacore.io.mem_port2, DataMem.io.port)
    }

    if (MARCH == "P"){
        val memory  = Module(new TimingRamAXI)
        val arbiter = Module(new AXI4ArbiterNto1(2))
        
        memory.io.clock := clock
        memory.io.reset := reset
        arbiter.io.in(0)  <> erythrinacore.io.mem_port1
        arbiter.io.in(1)  <> erythrinacore.io.mem_port2

        //arbiter.io.out  <> memory.io.port
        val addr_space  = List(
            (0x02000000L.U, 0x0200ffffL.U),     // clint
            (0x0f000000L.U, 0xffffffffL.U)      // other
        )
        val xbar        = Module(new AXI4XBar1toN(addr_space))
        xbar.io.in      <> arbiter.io.out
        //DelayConnect(xbar.io.out(0), memory.io.port)
        //DelayConnect(xbar.io.out(1), axi4clint.io)
        //DelayConnect(xbar.io.out(2), axi4uart.io)
        xbar.io.out(1)  <> memory.io.port
        xbar.io.out(0)  <> axi4clint.io
    }

    // commit
    val commit = Module(new CommitWrapper)
    commit.io.clock := clock
    commit.io.reset := reset
    commit.io.port  <> erythrinacore.io.InstCommit

}
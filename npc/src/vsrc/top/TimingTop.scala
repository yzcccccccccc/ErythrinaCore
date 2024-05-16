package top

import chisel3._
import bus.mem._
import erythcore._
import bus.axi4._
import device._

// this module is mainly for analyzing the timing performance of the core
class TimingTop extends Module with ErythrinaDefault{
    val io = IO(new AXI4)

    ErythrinaSetting.isSTA = true
    val erythrinacore = Module(new ErythrinaCore)

    val axi4clint   = Module(new AXI4CLINT)

    if (MARCH == "H"){
        assert(0.B, "Not implemented yet")
    }

    if (MARCH == "P"){
        val arbiter = Module(new AXI4ArbiterNto1(2))
        
        arbiter.io.in(0)  <> erythrinacore.io.mem_port2
        arbiter.io.in(1)  <> erythrinacore.io.mem_port1

        //arbiter.io.out  <> memory.io.port
        val addr_space  = List(
            (0x02000000L.U, 0x0200ffffL.U),     // clint
            (0x0f000000L.U, 0xffffffffL.U)      // other
        )
        val xbar        = Module(new AXI4XBar1toN(addr_space))
        xbar.io.in      <> arbiter.io.out
        xbar.io.out(1)  <> io
        xbar.io.out(0)  <> axi4clint.io
    }

    // commit
    val commit = Module(new CommitWrapper)
    commit.io.clock := clock
    commit.io.reset := reset
    commit.io.port  <> erythrinacore.io.InstCommit

}
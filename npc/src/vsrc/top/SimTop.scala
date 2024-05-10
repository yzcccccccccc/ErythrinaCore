package top

import chisel3._
import erythcore._
import bus.axi4._
import device._
import bus.mem._

// thie module is mainly for analyzing the corectness of the core
class SimTop extends Module with ErythrinaDefault{
    val erythcore = Module(new ErythrinaCore)

    val arbiter = Module(new AXI4ArbiterNto1(2))
    arbiter.io.in(0)    <> erythcore.io.mem_port1
    arbiter.io.in(1)    <> erythcore.io.mem_port2

    val clint = Module(new AXI4CLINT)

    val addr_space  = List(
        (0x02000000L.U, 0x0200ffffL.U),     // clint
        (0x0f000000L.U, 0xffffffffL.U)      // other
    )

    val mem = Module(new SimpleRamAXI)
    mem.io.clock := clock
    mem.io.reset := reset

    val xbar    = Module(new AXI4XBar1toN(addr_space))
    xbar.io.in      <> arbiter.io.out
    xbar.io.out(0)  <> clint.io
    xbar.io.out(1)  <> mem.io.port

    // commit
    val commit = Module(new CommitWrapper)
    commit.io.clock := clock
    commit.io.reset := reset
    commit.io.port  <> erythcore.io.InstCommit
}
package top

import chisel3._
import bus.axi4._
import bus.ivybus._
import erythcore._
import device._

class ysyx_1919810 extends Module{
    val io = IO(new AXI4)

    val erythrinacore = Module(new ErythrinaCore)

    val arbiter = Module(new AXI4ArbiterNto1(2))
    arbiter.io.in(0)    <> erythrinacore.io.mem_port1
    arbiter.io.in(1)    <> erythrinacore.io.mem_port2

    val clint = Module(new AXI4CLINT)

    val addr_space  = List(
        (0x02000000L.U, 0x0200ffffL.U),     // clint
        (0x0f000000L.U, 0x0fffffffL.U)      // other
    )
    val xbar    = Module(new AXI4XBar1toN(addr_space))
    xbar.io.in      <> arbiter.io.out
    xbar.io.out(0)  <> clint.io
    xbar.io.out(1)  <> io
}
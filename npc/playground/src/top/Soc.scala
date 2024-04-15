package top

import chisel3._
import bus.mem._
import utils.LatencyPipeRis
import erythcore._
import bus.axi4._
import device._
import chisel3.ltl.Delay

class Soc extends Module with ErythrinaDefault{
    val io_commit = IO(new ErythrinaCommit)

    val erythrinacore = Module(new ErythrinaCore)

    val axi4clint   = Module(new AXI4CLINTSim)
    val axi4uart    = Module(new AXI4UartSim)

    erythrinacore.io.InstCommit <> io_commit

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
        val memory  = Module(new SimpleRamAXI)
        val arbiter = Module(new AXI4ArbiterNto1(2))
        
        memory.io.clock := clock
        memory.io.reset := reset
        arbiter.io.in(0)  <> erythrinacore.io.mem_port1
        arbiter.io.in(1)  <> erythrinacore.io.mem_port2

        //arbiter.io.out  <> memory.io.port
        val addr_space  = List(
            (0x80000000L.U, 0x8fffffffL.U),         // memory
            (0xa0000048L.U, 0xa0000050L.U),         // clint-sim
            (0xa00003f8L.U, 0xa00003ffL.U)          // uart-sim
        )
        val xbar        = Module(new AXI4XBar1toN(addr_space))
        xbar.io.in      <> arbiter.io.out
        DelayConnect(xbar.io.out(0), memory.io.port)
        DelayConnect(xbar.io.out(1), axi4clint.io)
        DelayConnect(xbar.io.out(2), axi4uart.io)
        //xbar.io.out(0)  <> memory.io.port
        //xbar.io.out(1)  <> axi4clint.io
        //xbar.io.out(2)  <> axi4uart.io
    }

}
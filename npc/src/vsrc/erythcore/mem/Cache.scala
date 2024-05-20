package erythcore.mem

import chisel3._
import chisel3.util._
import bus.ivybus.IvyBus
import bus.axi4.AXI4

case class CacheConfig(
    ro: Boolean = true,
    name: String = "cache",
    tagbits: Int = 12,      // 12 bits
    indexbits: Int = 8,     // 8 bits

    metabytes: Int = 8,     // 8 bytes, 64 bits
    ways: Int = 4,          // 4 ways
)

class CacheIO extends Bundle{
    val io_cpu = Flipped(new IvyBus)
    val io_mem = new AXI4
}
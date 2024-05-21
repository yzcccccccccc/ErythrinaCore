package erythcore.mem

import chisel3._
import chisel3.util._
import bus.ivybus.IvyBus
import bus.axi4.AXI4
import erythcore.ErythrinaDefault

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

class CacheBpBundle extends Bundle with ErythrinaDefault{         // bypass signals
    val is_bypass       = Bool()
    val bypass_wen      = Bool()
    val bypass_en       = Bool()
    val bypass_data     = UInt(XLEN.W)
    val bypass_addr     = UInt(XLEN.W)
    val bypass_strb     = UInt(MASKLEN.W)
}

class CacheStage1ToStage2(implicit cacheConfig: CacheConfig) extends Bundle with ErythrinaDefault{
}

class CacheStage2ToStage3(implicit cacheConfig: CacheConfig) extends Bundle with ErythrinaDefault{
}

class CacheStage1IO(implicit cacheConfig: CacheConfig) extends Bundle{
    val io_cpu      = Flipped(new IvyBus)
    val s1_s2_zip   = Decoupled(new CacheStage1ToStage2)
}

class CacheStage1(implicit cacheConfig: CacheConfig) extends Module{

}
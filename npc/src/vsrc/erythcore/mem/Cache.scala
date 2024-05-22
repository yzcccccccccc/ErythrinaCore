package erythcore.mem

import chisel3._
import chisel3.util._
import bus.axi4._
import bus.ivybus._
import erythcore.ErythrinaDefault
import chisel3.util.random.LFSR
import utils.LookupTreeDefault
import utils.LookupTree
import utils.MaskExpand
import erythcore.CSRnum.mstatus

/*  
    Raw information of cache
    ---------------------------------------------------------------
    way0
    |   valid   |   dirty   |   tag     |   data                |
    |   1 bit   |   1 bit   |   23 bits |   256 bits(32 Bytes)  |
    ---------------------------------------------------------------
    way1
    ...
    ---------------------------------------------------------------
    way2
    ...
    ---------------------------------------------------------------
    way3
    ...
    ---------------------------------------------------------------

    Cutting data(32 Bytes) into 8 banks, each bank 4 Bytes

    Total 4 * 8 * 32 = 1024 Bytes = 1KB
    4 ways, each way 8 entries, each entry 32 Bytes
    
    Index:  3 bits (log2(8))
    Offset: 5 bits (log2(32))
    Tag:    24 bits
*/

case class CacheConfig(
    name: String = "cache",
    // [tag] [index] [offset]
    tagbits: Int = 24,
    indexbits: Int = 3,
    offsetbits: Int = 5,

    // Cache size
    ways: Int = 4,
    sets: Int = 8,
    blocksize: Int = 32,    // 32 Bytes
    banknum: Int = 8,       // 32 Bytes / 4 Bytes = 8

    // Cache range
    cacheable_lbound: Int = 0xa0000000,
    cacheable_ubound: Int = 0xbfffffff
)

class CacheIO(implicit cacheConfig:CacheConfig) extends Bundle {
    val cpu_port    = Flipped(new IvyBus())
    val mem_port    = new AXI4
}

class Cache(implicit cacheConfig:CacheConfig) extends Module with ErythrinaDefault{
    val io = IO(new CacheIO)

    val cpu_port = io.cpu_port
    val mem_port = io.mem_port

    def get_bank(offset : UInt) = {
        offset(cacheConfig.offsetbits - 1, 3)
    }

    /* ---------- Cache Memory Def ---------- */
    // valid
    val cache_v = SyncReadMem(cacheConfig.ways * cacheConfig.sets, Bool())    // 4 way * 8 entry
    when (reset.asBool){
        for (i <- 0 until cacheConfig.ways * cacheConfig.sets){
            cache_v.write(i.U, false.B)
        }
    }

    // dirty
    val cache_d = SyncReadMem(cacheConfig.ways * cacheConfig.sets, Bool())    // 4 way * 8 entry
    when (reset.asBool){
        for (i <- 0 until cacheConfig.ways * cacheConfig.sets){
            cache_d.write(i.U, false.B)
        }
    }

    // tag
    val cache_t = SyncReadMem(cacheConfig.ways * cacheConfig.sets, UInt(cacheConfig.tagbits.W))    // 4 way * 8 entry

    // cacheline
    val cache_c = SyncReadMem(cacheConfig.ways * cacheConfig.sets, Vec(cacheConfig.banknum, UInt(XLEN.W)))    // 4 way * 8 entry

    /* ---------- Cache Important Signals ---------- */
    val isbypass    = Wire(Bool())          // valid in IDLE stage
    val ishit       = Wire(Bool())          // valid in LOOKUP stage

    val tag         = cpu_port.req.bits.addr(cacheConfig.tagbits + cacheConfig.indexbits + cacheConfig.offsetbits - 1, cacheConfig.indexbits + cacheConfig.offsetbits)
    val index       = cpu_port.req.bits.addr(cacheConfig.indexbits + cacheConfig.offsetbits - 1, cacheConfig.offsetbits)
    val offset      = cpu_port.req.bits.addr(cacheConfig.offsetbits - 1, 2)

    val tag_r       = RegEnable(tag, cpu_port.req.fire)
    val index_r     = RegEnable(index, cpu_port.req.fire)
    val offset_r    = RegEnable(offset, cpu_port.req.fire)
    
    val victim_way      = RegEnable(LFSR(log2Ceil(cacheConfig.ways)), cpu_port.req.fire)
    val victim_dat_vec  = RegInit(VecInit(Seq.fill(cacheConfig.banknum)(0.U(XLEN.W))))
    val isvicdirty       = Wire(Bool())          // victim cacheline is dirty

    val cpu_req_r   = RegEnable(cpu_port.req.bits, cpu_port.req.fire)
    val rd_dat_vec  = RegInit(VecInit(Seq.fill(cacheConfig.banknum)(0.U(XLEN.W))))      // cache read data

    /*  ---------- Main FSM ---------- */
    val sIDLE :: sLOOKUP :: sHIT_WAIT :: sMISS_WAIT :: sBP_WAIT :: Nil = Enum(5)
    val m_state = RegInit(sIDLE)
    switch (m_state){
        is (sIDLE){
            when (cpu_port.req.fire){
                m_state := Mux(isbypass, sBP_WAIT, sLOOKUP)
            }
        }
        is (sLOOKUP){
            when (ishit){
                m_state := Mux(cpu_port.resp.fire, sIDLE, sHIT_WAIT)
            }.otherwise{
                m_state := sMISS_WAIT
            }
        }
        is (sHIT_WAIT){
            when (cpu_port.resp.fire){
                m_state := sIDLE
            }
        }
        is (sMISS_WAIT){
            when (cpu_port.resp.fire){
                m_state := sIDLE
            }
        }
        is (sBP_WAIT){
            when (cpu_port.resp.fire){
                m_state := sIDLE
            }
        }
    }

    /* ---------- AXI_R FSM ---------- */
    val axi_r_bp    = m_state === sIDLE & cpu_port.req.fire & isbypass & ~cpu_port.req.bits.wen
    val axi_r_miss  = m_state === sLOOKUP & ~ishit
    val axi_r_en    = axi_r_bp | axi_r_miss

    val axi_r_bp_r      = Reg(Bool())
    val axi_r_miss_r    = Reg(Bool())

    when (axi_r_en){
        axi_r_bp_r      := axi_r_bp
        axi_r_miss_r    := axi_r_miss
    }.elsewhen(r_state === sR_RECV & mem_port.r.fire & mem_port.r.bits.last){
        axi_r_bp_r      := 0.B
        axi_r_miss_r    := 0.B
    }

    val sR_IDLE :: sR_REQ :: sR_RECV :: Nil = Enum(3)
    val r_state = RegInit(sR_IDLE)
    switch (r_state){
        is (sR_IDLE){
            when (axi_r_en){
                r_state := sR_REQ
            }
        }
        is (sR_REQ){
            when (mem_port.ar.fire){
                r_state := sR_RECV
            }
        }
        is (sR_RECV){
            when (mem_port.r.fire & mem_port.r.bits.last){
                r_state := sR_IDLE
            }
        }
    }

    // AR
    val ar_addr_r   = Reg(UInt(XLEN.W))
    when (axi_r_en){
        ar_addr_r   := Mux1H(Seq(
            axi_r_bp_r -> cpu_port.req.bits.addr,
            axi_r_miss_r -> Cat(cpu_req_r.addr(XLEN - 1, 2), 0.U(2.W))   // 4 Bytes aligned
        ))
    }

    mem_port.ar.valid       := r_state === sR_REQ
    mem_port.ar.bits.addr   := ar_addr_r
    mem_port.ar.bits.len    := Mux(axi_r_bp_r, 0.U, cacheConfig.banknum.U)
    mem_port.ar.bits.size   := Mux(axi_r_bp_r, cpu_req_r.size, log2Ceil(cacheConfig.blocksize / cacheConfig.banknum).U - 1.U)
    mem_port.ar.bits.burst  := Mux(axi_r_bp_r, AXI4Parameters.BURST_FIXED, AXI4Parameters.BURST_WRAP)

    // R
    val rd_use_high = Reg(Bool())
    val rd_ptr      = Reg(UInt(log2Ceil(cacheConfig.banknum).W))
    val rd_end      = Reg(UInt(log2Ceil(cacheConfig.banknum).W))
    when (mem_port.ar.fire){
        rd_use_high := cpu_req_r.addr(2)
        rd_ptr      := get_bank(offset_r)
        rd_end      := get_bank(offset_r) - 1.U
    }
    when (mem_port.r.fire){
        rd_use_high := ~rd_use_high
        rd_ptr      := rd_ptr + 1.U
    }

    val mem_rd_data = Mux(rd_use_high, mem_port.r.bits.data(63, 32), mem_port.r.bits.data(31, 0))
    val cpu_wb_strb = MaskExpand(Mux(cpu_req_r.addr(2), cpu_req_r.mask(7, 4), cpu_req_r.mask(3, 0)))
    val cpu_wb_data = mem_rd_data & ~cpu_wb_strb | cpu_req_r.data & cpu_wb_strb
    when (mem_port.r.fire){
        rd_dat_vec(rd_ptr) := Mux(rd_ptr === offset_r, cpu_wb_data, mem_rd_data)
    }

    /* ---------- AXI_W FSM ---------- */
    val axi_w_bp    = m_state === sIDLE & cpu_port.req.fire & isbypass & cpu_port.req.bits.wen
    val axi_w_miss  = m_state === sLOOKUP & ~ishit & isvicdirty
    val axi_w_en    = axi_w_bp | axi_w_miss

    val aw_has_fire = Reg(Bool())
    val w_has_fire  = Reg(Bool())

    when (mem_port.aw.fire){
        aw_has_fire := 1.B
    }.elsewhen(mem_port.b.fire){
        aw_has_fire := 0.B
    }

    when (mem_port.w.fire & mem_port.w.bits.last){
        w_has_fire := 1.B
    }.elsewhen(mem_port.b.fire){
        w_has_fire := 0.B
    }

    val aw_has_done = aw_has_fire | mem_port.aw.fire
    val w_has_done  = w_has_fire | mem_port.w.fire & mem_port.w.bits.last

    val sW_IDLE :: sW_REQ :: sW_RESP :: Nil = Enum(3)
    val w_state = RegInit(sW_IDLE)
    switch (w_state){
        is (sW_IDLE){
            when (axi_w_en){
                w_state := sW_REQ
            }
        }
        is (sW_REQ){
            when (aw_has_done & w_has_done){
                w_state := sW_RESP
            }
        }
        is (sW_RESP){
            when (mem_port.b.fire){
                w_state := sW_IDLE
            }
        }
    }

    // AW
    val aw_addr_r   = Reg(UInt(XLEN.W))
    when (axi_w_en){
        aw_addr_r   := Mux1H(Seq(
            axi_w_bp -> cpu_port.req.bits.addr,
            axi_w_miss -> Cat(cpu_req_r.addr(XLEN - 1, 2), 0.U(2.W))   // 4 Bytes aligned
        ))
    }

    mem_port.aw.valid       := w_state === sW_REQ
    mem_port.aw.bits.addr   := aw_addr_r
    mem_port.aw.bits.len    := Mux(axi_w_bp, 0.U, cacheConfig.banknum.U)
    mem_port.aw.bits.size   := Mux(axi_w_bp, cpu_req_r.size, log2Ceil(cacheConfig.blocksize / cacheConfig.banknum).U - 1.U)
    mem_port.aw.bits.burst  := Mux(axi_w_bp, AXI4Parameters.BURST_FIXED, AXI4Parameters.BURST_WRAP)

    // W
    val wr_use_high = Reg(Bool())
    val wr_ptr      = Reg(UInt(log2Ceil(cacheConfig.banknum).W))
    val wr_end      = Reg(UInt(log2Ceil(cacheConfig.banknum).W))
    when (axi_w_en){
        wr_use_high := cpu_port.req.bits.addr(2)
        wr_ptr      := get_bank(offset_r)
        wr_end      := get_bank(offset_r) - 1.U
    }
    when (mem_port.w.fire){
        wr_use_high := ~wr_use_high
        wr_ptr      := wr_ptr + 1.U
    }

    val wr_data = Mux(axi_w_bp, cpu_req_r.data, victim_dat_vec(wr_ptr))
    val wr_strb = Mux(axi_w_bp, cpu_req_r.mask, "b1111".U)

    mem_port.w.valid        := w_state === sW_REQ
    mem_port.w.bits.data    := Mux(wr_use_high, Cat(wr_data, 0.U(XLEN.W)), Cat(0.U(XLEN.W), wr_data))
    mem_port.w.bits.strb    := Mux(wr_use_high, Cat(wr_strb, 0.U(MASKLEN.W)), Cat(0.U(MASKLEN.W), wr_strb))
    mem_port.w.bits.last    := wr_ptr === wr_end

    // B
    mem_port.b.ready    := w_state === sW_RESP

    /* ---------- Cache Read ---------- */
    val cache_query = m_state === sIDLE & cpu_port.req.fire
    val valid_vec   = VecInit((0 until cacheConfig.ways).map(i => cache_v.read((i * cacheConfig.sets).U + index, cache_query)))
    val dirty_vec   = VecInit((0 until cacheConfig.ways).map(i => cache_d.read((i * cacheConfig.sets).U + index, cache_query)))
    val tag_vec     = VecInit((0 until cacheConfig.ways).map(i => cache_t.read((i * cacheConfig.sets).U + index, cache_query)))
    val dat_vec     = VecInit((0 until cacheConfig.ways).map(i => cache_c.read((i * cacheConfig.sets).U + index, cache_query)))

    val hit_vec     = VecInit((0 until cacheConfig.ways).map(i => valid_vec(i) & tag_vec(i) === tag))
    val hit_way     = PriorityEncoder(hit_vec)
    ishit           := hit_vec.reduce(_ | _)

    when (m_state === sLOOKUP & ~ishit){
        victim_dat_vec  := dat_vec(victim_way)
    }

    /* ---------- Cache Write ---------- */
    val cache_hit_update    = m_state === sLOOKUP & ishit & cpu_req_r.wen
    val cache_miss_update   = RegNext(m_state === sMISS_WAIT & mem_port.r.fire & mem_port.r.bits.last & axi_r_miss_r)
    
    val cache_hit_wb_ptr    = get_bank(offset)
    val cache_hit_wb_strb   = MaskExpand(cpu_req_r.mask)
    val cache_hit_wb_data   = cache_hit_wb_strb & cpu_req_r.data | ~cache_hit_wb_strb & dat_vec(hit_way)(cache_hit_wb_ptr)
    val cache_hit_wb_vec    = VecInit((0 until cacheConfig.banknum).map(i => Mux(i.U === cache_hit_wb_ptr, cache_hit_wb_data, dat_vec(hit_way)(i))))
    when (cache_hit_update){
        cache_d.write(hit_way * cacheConfig.sets.U + index, true.B)
        cache_c.write(hit_way * cacheConfig.sets.U + index, cache_hit_wb_vec)
    }

    when (cache_miss_update){
        cache_v.write(victim_way * cacheConfig.sets.U + index_r, true.B)
        cache_d.write(victim_way * cacheConfig.sets.U + index_r, true.B)
        cache_t.write(victim_way * cacheConfig.sets.U + index_r, tag_r)
        cache_c.write(victim_way * cacheConfig.sets.U + index_r, rd_dat_vec)
    }

    /* ---------- cpu port ---------- */
    // Req
    cpu_port.req.ready := m_state === sIDLE && r_state === sR_IDLE && w_state === sW_IDLE

    // Res
    // hit res
    val hit_ptr     = get_bank(offset_r)
    val hit_res_r   = RegEnable(dat_vec(hit_way)(hit_ptr), m_state === sLOOKUP & ishit)
    val hit_res     = Mux(m_state === sLOOKUP, dat_vec(hit_way)(hit_ptr), hit_res_r)

    // miss res
    val miss_axi_r_has_resp  = Reg(Bool())
    when (mem_port.r.fire & ~mem_port.r.bits.last & m_state === sMISS_WAIT){
        miss_axi_r_has_resp  := 1.B
    }
    when (m_state === sLOOKUP){
        miss_axi_r_has_resp  := 0.B
    }

    val miss_mem_res    = Mux(cpu_req_r.addr(2), mem_port.r.bits.data(63, 32), mem_port.r.bits.data(31, 0))
    val miss_res        = RegEnable(miss_mem_res, mem_port.r.fire & ~miss_axi_r_has_resp)

    // bypass res
    val bypass_axi_r_has_resp   = Reg(Bool())
    when (mem_port.r.fire & ~mem_port.r.bits.last & m_state === sBP_WAIT){
        bypass_axi_r_has_resp   := 1.B
    }
    when (m_state === sIDLE){
        bypass_axi_r_has_resp   := 0.B
    }

    val bypass_mem_res  = Mux(cpu_req_r.addr(2), mem_port.r.bits.data(63, 32), mem_port.r.bits.data(31, 0))
    val bypass_res_r    = RegEnable(bypass_mem_res, mem_port.r.fire & ~bypass_axi_r_has_resp)
    val bypass_res      = Mux(bypass_axi_r_has_resp, bypass_res_r, bypass_mem_res)

    // Resp
    val hit_valid       = m_state === sLOOKUP & ishit | m_state === sHIT_WAIT
    val miss_valid      = m_state === sMISS_WAIT & miss_axi_r_has_resp
    val bypass_valid    = m_state === sBP_WAIT & (bypass_axi_r_has_resp | mem_port.r.fire)
    cpu_port.resp.valid     := hit_valid | miss_valid | bypass_valid
    cpu_port.resp.bits.data := Mux1H(Seq(
        hit_valid       -> hit_res,
        miss_valid      -> miss_res,
        bypass_valid    -> bypass_res
    ))
    cpu_port.resp.bits.resp := 0.U
}
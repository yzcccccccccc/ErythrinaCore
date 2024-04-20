package bus.ivybus

// Convertor!
import chisel3._
import chisel3.util._
import bus.axi4._
import erythcore.ErythrinaDefault
import utils.LookupTreeDefault
import utils.LatencyPipeBit

// convert a Ivy request to AXI4
class Ivy2AXI4[T <: AXI4Lite](_type: T = new AXI4) extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val in  = Flipped(new IvyBus)
        val out = Flipped(Flipped(_type))
    })

    val aw_has_fire = Reg(Bool())
    val w_has_fire  = Reg(Bool())

    // FSM
    val sIDLE :: sR :: sW :: Nil = Enum(3)
    val state = RegInit(sIDLE)

    switch (state){
        is (sIDLE){
            when (io.out.ar.fire){
                state   := sR
            }.elsewhen((io.out.aw.fire | aw_has_fire) & (io.out.w.fire | w_has_fire)){
                state   := sW
            }
        }
        is (sR){
            when (io.out.r.fire){
                state   := sIDLE
            }
        }
        is (sW){
            when (io.out.b.fire){
                state   := sIDLE
            }
        }
    }
    
    // Fire Reg
    when (io.out.aw.fire){
        aw_has_fire := 1.B
    }

    when (io.out.w.fire){
        w_has_fire  := 1.B
    }

    when (state === sW){
        aw_has_fire := 0.B
        w_has_fire  := 0.B
    }

    // AXI Read
    io.out.ar.valid     := LatencyPipeBit(state === sIDLE && io.in.req.valid && ~io.in.req.bits.wen, LATENCY)
    io.out.ar.bits.addr := io.in.req.bits.addr
    val ar_addr_r   = RegEnable(io.out.ar.bits.addr, io.out.ar.fire)

    io.out.r.ready      := LatencyPipeBit(state === sR && io.in.resp.ready, LATENCY)

    // AXI Write
    io.out.aw.valid     := LatencyPipeBit(state === sIDLE && io.in.req.valid && io.in.req.bits.wen, LATENCY)
    io.out.aw.bits.addr := io.in.req.bits.addr

    val strb = Mux(io.in.req.bits.addr(2), Cat(io.in.req.bits.mask, Fill(MASKLEN, 0.B)), Cat(Fill(MASKLEN, 0.B), io.in.req.bits.mask))
    val data = Mux(io.in.req.bits.addr(2), Cat(io.in.req.bits.data, Fill(XLEN, 0.B)), Cat(Fill(XLEN, 0.B), io.in.req.bits.data))
    io.out.w.valid      := LatencyPipeBit(state === sIDLE && io.in.req.valid && io.in.req.bits.wen, LATENCY)
    io.out.w.bits.data  := (if (_type.getClass() == classOf[AXI4]) data else io.in.req.bits.data)
    io.out.w.bits.strb  := (if (_type.getClass() == classOf[AXI4]) strb else io.in.req.bits.mask)

    io.out.b.ready      := LatencyPipeBit(state === sW && io.in.resp.ready, LATENCY)

    // AXI4 Specific
    if (_type.getClass() == classOf[AXI4]){
        val axi4 = io.out.asInstanceOf[AXI4]
        
        // TODO TBD
        // AR
        axi4.ar.bits.id     := "h01".U
        axi4.ar.bits.len    := "h00".U
        axi4.ar.bits.size   := io.in.req.bits.size
        axi4.ar.bits.burst  := AXI4Parameters.BURST_FIXED

        // AW
        axi4.aw.bits.id     := "h01".U
        axi4.aw.bits.len    := "h00".U
        axi4.aw.bits.size   := io.in.req.bits.size
        axi4.aw.bits.burst  := AXI4Parameters.BURST_FIXED

        // W
        axi4.w.bits.last            := 1.B

        // R, B, ...
    }

    // IvyBus
    io.in.req.ready         := state === sIDLE & Mux(io.in.req.bits.wen, io.out.aw.ready, io.out.ar.ready)
    io.in.resp.valid        := (state === sR && io.out.r.valid) | (state === sW && io.out.b.valid)
    io.in.resp.bits.data    := Mux(ar_addr_r(2), io.out.r.bits.data(63, 32), io.out.r.bits.data(31, 0))
    io.in.resp.bits.resp    := Mux(state === sW, io.out.b.bits.resp, io.out.r.bits.resp)
}

object Ivy2AXI4{
    def apply[T <: AXI4Lite](in: IvyBus, _type: T) = {
        val bridge = Module(new Ivy2AXI4(_type))
        bridge.io.in    <> in
        bridge.io.out
    }
}

// TODO: improve efficiency
// convert a AXI request to Ivy request
class AXI42Ivy[T <: AXI4Lite](_type: T = new AXI4) extends Module with ErythrinaDefault{
    val io = IO(new Bundle {
        val in  = Flipped(_type)
        val out = new IvyBus
    })

    // FSM
    val sARW :: sR :: sW :: sB ::Nil = Enum(4)
    val state   = RegInit(sARW)

    switch (state){
        is (sARW){
            when (io.in.ar.fire){
                state   := sR
            }.elsewhen(io.in.aw.fire){
                state   := sW
            }
        }
        is (sR){
            when (io.in.r.fire){
                state   := sARW
            }
        }
        is (sW){
            when (io.in.w.fire){
                state   := sB
            }
        }
        is (sB){
            when (io.in.b.fire){
                state   := sARW
            }
        }
    }

    // AXI-Read
    io.in.ar.ready      := LatencyPipeBit(state === sARW & io.out.req.ready, LATENCY)

    io.in.r.valid       := LatencyPipeBit(state === sR & io.out.resp.valid, LATENCY)
    io.in.r.bits.data   := (if (_type.getClass() == classOf[AXI4]) Cat(Fill(XLEN, 0.B), io.out.resp.bits.data) else io.out.resp.bits.data)
    io.in.r.bits.resp   := io.out.resp.bits.resp

    // AXI-Write
    io.in.aw.ready      := LatencyPipeBit(state === sARW, LATENCY)

    io.in.w.ready       := LatencyPipeBit(state === sW & io.out.req.ready, LATENCY)

    val w_resp_r    = RegEnable(io.out.resp.bits.resp, io.in.w.fire)
    io.in.b.valid       := LatencyPipeBit(state === sB, LATENCY)
    io.in.b.bits.resp   := w_resp_r

    if (_type.getClass() == classOf[AXI4]){
        val axi4 = io.in.asInstanceOf[AXI4]
        
        // R
        axi4.r.bits.id              := "h01".U
        axi4.r.bits.last            := 0.B

        // B
        axi4.b.bits.id              := "h01".U
    }

    // IvyBus
    io.out.req.valid        := (state === sARW & io.in.ar.valid) | (state === sW & io.in.w.valid)
    io.out.req.bits.wen     := state === sW
    
    val w_addr_r    = RegEnable(io.in.aw.bits.addr, io.in.aw.fire)
    val w_size_r    = RegEnable((if (_type.getClass() == classOf[AXI4]) io.in.asInstanceOf[AXI4].aw.bits.size else "b010".U), io.in.aw.fire)
    val r_size      = (if (_type.getClass() == classOf[AXI4]) io.in.asInstanceOf[AXI4].ar.bits.size else "b010".U)
    io.out.req.bits.addr    := Mux(state === sARW, io.in.ar.bits.addr, w_addr_r)
    io.out.req.bits.data    := io.in.w.bits.data(31, 0)
    io.out.req.bits.mask    := io.in.w.bits.strb(3, 0)
    io.out.req.bits.size    := Mux(state === sARW, r_size, w_size_r)

    io.out.resp.ready       := LookupTreeDefault(state, 0.B, List(
        sB  -> io.in.b.ready,
        sR  -> io.in.r.ready
    ))
}

object AXI42Ivy{
    def apply[T <: AXI4Lite](in : T, _type: T) = {
        val bridge = Module(new AXI42Ivy(_type))
        bridge.io.in    <> in
        bridge.io.out
    }
}
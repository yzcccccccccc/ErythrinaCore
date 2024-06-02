package bus.ivybus

// Convertor!
import chisel3._
import chisel3.util._
import bus.axi4._
import erythcore._
import utils.LookupTreeDefault
import utils.LatencyPipeBit

// convert a Ivy request to AXI4
class Ivy2AXI4[T <: AXI4Lite](_type: T = new AXI4) extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val in  = Flipped(new IvyBus)
        val out = Flipped(Flipped(_type))
    })

    val has_aw_fire = RegInit(false.B)
    val has_w_fire  = RegInit(false.B)

    when (io.out.aw.fire){
        has_aw_fire := true.B
    }.elsewhen(io.out.w.fire){
        has_w_fire := true.B
    }.elsewhen(io.out.b.fire){
        has_aw_fire := false.B
        has_w_fire  := false.B
    }

    // FSM
    val sIDLE :: sR_req :: sR_recv :: sW_req :: sW_recv :: Nil = Enum(5)
    val state = RegInit(sIDLE)
    switch (state){
        is (sIDLE){
            when (io.in.req.fire){
                state := Mux(io.in.req.bits.wen, sW_req, sR_req)
            }
        }
        is (sR_req){
            when (io.out.ar.fire){
                state := sR_recv
            }
        }
        is (sR_recv){
            when (io.in.resp.fire){
                state := sIDLE
            }
        }
        is (sW_req){
            when ((io.out.aw.fire | has_aw_fire) & (io.out.w.fire | has_w_fire)){
                state := sW_recv
            }
        }
        is (sW_recv){
            when (io.in.resp.fire){
                state := sIDLE
            }
        }
    }

    val req_info = RegEnable(io.in.req.bits, io.in.req.fire)

    // AXI-Read
    io.out.ar.valid     := LatencyPipeBit(state === sR_req, LATENCY)
    io.out.ar.bits.addr := req_info.addr

    io.out.r.ready      := LatencyPipeBit(state === sR_recv & io.in.resp.ready, LATENCY)

    // AXI-Write
    io.out.aw.valid     := LatencyPipeBit(state === sW_req & ~has_aw_fire, LATENCY)
    io.out.aw.bits.addr := req_info.addr

    val strb = Mux(req_info.addr(2), Cat(req_info.mask, Fill(MASKLEN, 0.B)), Cat(Fill(MASKLEN, 0.B), req_info.mask))
    val data = Mux(req_info.addr(2), Cat(req_info.data, Fill(XLEN, 0.B)), Cat(Fill(XLEN, 0.B), req_info.data))
    io.out.w.valid      := LatencyPipeBit(state === sW_req & ~has_w_fire, LATENCY)
    io.out.w.bits.data  := (if (_type.getClass() == classOf[AXI4]) data else req_info.data)
    io.out.w.bits.strb  := (if (_type.getClass() == classOf[AXI4]) strb else req_info.mask)

    io.out.b.ready      := LatencyPipeBit(state === sW_recv & io.in.resp.ready, LATENCY)

    // AXI4 specific
    if (_type.getClass() == classOf[AXI4]){
        val axi4 = io.out.asInstanceOf[AXI4]

        // AR
        axi4.ar.bits.id     := "h01".U
        axi4.ar.bits.len    := "h00".U
        axi4.ar.bits.size   := req_info.size
        axi4.ar.bits.burst  := AXI4Parameters.BURST_FIXED

        // AW
        axi4.aw.bits.id     := "h01".U
        axi4.aw.bits.len    := "h00".U
        axi4.aw.bits.size   := req_info.size
        axi4.aw.bits.burst  := AXI4Parameters.BURST_FIXED

        // W
        axi4.w.bits.last    := 1.B

        // R, B ...
    }

    // IvyBus
    io.in.req.ready         := state === sIDLE
    io.in.resp.valid        := LookupTreeDefault(state, 0.B, List(
        sR_recv -> io.out.r.valid,
        sW_recv -> io.out.b.valid
    ))
    io.in.resp.bits.data    := Mux(req_info.addr(2), io.out.r.bits.data(63, 32), io.out.r.bits.data(31, 0))
    io.in.resp.bits.resp    := LookupTreeDefault(state, 0.U, List(
        sR_recv -> io.out.r.bits.resp,
        sW_recv -> io.out.b.bits.resp
    ))
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
class AXI42Ivy[T <: AXI4Lite](_type: T = new AXI4) extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val in  = Flipped(_type)
        val out = new IvyBus
    })

    val aw_has_fire = Reg(Bool())
    val w_has_fire  = Reg(Bool())

    // FSM
    val sIDLE :: sR :: sW :: sB ::Nil = Enum(4)
    val state   = RegInit(sIDLE)

    switch (state){
        is (sIDLE){
            when (io.in.ar.fire){
                state   := sR
            }.elsewhen((io.in.aw.fire | aw_has_fire) & (io.in.w.fire | w_has_fire)){
                state   := sW
            }
        }
        is (sR){
            when (io.in.r.fire){
                state   := sIDLE
            }
        }
        is (sW){
            when (io.in.b.fire){
                state   := sIDLE
            }
        }
    }

    // AXI-Read
    io.in.ar.ready      := LatencyPipeBit(state === sIDLE & io.out.req.ready, LATENCY)
    val raddr_r     = RegEnable(io.in.ar.bits.addr, io.in.ar.fire)
    val rsize_r     = RegEnable(if (_type.getClass() == classOf[AXI4]) io.in.asInstanceOf[AXI4].ar.bits.size else "b010".U, io.in.ar.fire)

    io.in.r.valid       := LatencyPipeBit(state === sR & io.out.resp.valid, LATENCY)
    val rdata       = Mux(raddr_r(2), Cat(io.out.resp.bits.data, Fill(XLEN, 0.B)), Cat(Fill(XLEN, 0.B), io.out.resp.bits.data))
    io.in.r.bits.data   := (if (_type.getClass() == classOf[AXI4]) rdata else io.out.resp.bits.data)
    io.in.r.bits.resp   := io.out.resp.bits.resp

    // AXI-Write
    io.in.aw.ready      := LatencyPipeBit(state === sIDLE, LATENCY)

    io.in.w.ready       := LatencyPipeBit(state === sIDLE, LATENCY)

    val w_resp_r    = RegEnable(io.out.resp.bits.resp, io.in.w.fire)
    io.in.b.valid       := LatencyPipeBit(state === sW && io.out.resp.valid, LATENCY)
    io.in.b.bits.resp   := w_resp_r

    if (_type.getClass() == classOf[AXI4]){
        val axi4 = io.in.asInstanceOf[AXI4]
        
        // R
        axi4.r.bits.id              := "h01".U
        axi4.r.bits.last            := 1.B

        // B
        axi4.b.bits.id              := "h01".U
    }

    // IvyBus
    io.out.req.valid        := (state === sR) | (state === sIDLE & io.in.aw.valid & io.in.w.valid)
    io.out.req.bits.wen     := (state === sIDLE & io.in.aw.valid & io.in.w.valid)
    
    val waddr_r     = io.in.aw.bits.addr
    val wsize_r     =(if (_type.getClass() == classOf[AXI4]) io.in.asInstanceOf[AXI4].aw.bits.size else "b010".U)
    io.out.req.bits.addr    := Mux(state === sR, raddr_r, waddr_r)
    io.out.req.bits.data    := Mux(waddr_r(2), io.in.w.bits.data(63, 32), io.in.w.bits.data(31, 0))
    io.out.req.bits.mask    := Mux(waddr_r(2), io.in.w.bits.strb(7, 4), io.in.w.bits.strb(3, 0))
    io.out.req.bits.size    := Mux(state === sR, rsize_r, wsize_r)

    io.out.resp.ready       := LookupTreeDefault(state, 0.B, List(
        sW  -> io.in.b.ready,
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
package erythcore.common

import chisel3._
import chisel3.util._
import erythcore._
import bus.ivybus._
import erythcore.backend.fu.LSUOpType
import utils._


object SBstat{
    def invalid     = "b00".U
    def pending     = "b01".U
    def complete    = "b10".U
    def retired     = "b11".U

    def is_complete(stat:UInt):Bool = stat(1)
    def apply() = UInt(2.W)
}

class SBEntry extends Bundle with HasErythDefault{
  val stat  = SBstat()
  val addr  = UInt(XLEN.W)
  val data  = UInt(XLEN.W)
  val typ   = LSUOpType()
}

class LdBpBundle extends Bundle with HasErythDefault{
    val addr    = Input(UInt(XLEN.W))
    val hit     = Output(Bool())
    val data    = Output(UInt(XLEN.W))
}

class SBEnqBundle extends Bundle with HasErythDefault{
    val sb_entry    = Input(new SBEntry)
    val sb_idx      = Output(UInt(SBbits.W))
}

class SBCompleteBundle extends Bundle with HasErythDefault{
    val sb_idx  = Output(UInt(SBbits.W))
    val addr    = Output(UInt(XLEN.W))
    val data    = Output(UInt(XLEN.W))
}

class StoreBuffer extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val enq = Decoupled(new SBEnqBundle)

        // finish store calculation
        val complete_info   = Flipped(Decoupled(new SBCompleteBundle))

        // retire store
        val retire_info = Vec(2, Flipped(Decoupled(Input(UInt(SBbits.W)))))

        // store port (IvyBus)
        val st_port = new IvyBus

        // load bypass
        val load_bypass = new LdBpBundle
    })

    val sb  = Mem(NR_SB, new SBEntry)
    when (reset.asBool){
        for (i <- 0 until NR_SB){
            sb(i).stat := SBstat.invalid
        }
    }
    
    val head    = RegInit(0.U(SBbits.W))
    val tail    = RegInit(0.U(SBbits.W))
    val cnt     = RegInit(0.U((SBbits + 1).W))

    // Enqueue
    io.enq.ready := cnt < NR_SB.U
    when (io.enq.fire){
        sb(tail) := io.enq.bits.sb_entry
        io.enq.bits.sb_idx := tail
        tail := tail + 1.U
        cnt := cnt + 1.U
    }

    // retire
    for (i <- 0 until 2){
        io.retire_info(i).ready := cnt =/= 0.U
        when (io.retire_info(i).fire){
            sb(io.retire_info(i).bits).stat := SBstat.retired
            cnt := cnt - 1.U
        }
    }

    // complete
    io.complete_info.ready := cnt =/= 0.U
    when (io.complete_info.fire){
        sb(io.complete_info.bits.sb_idx).stat := SBstat.complete
        sb(io.complete_info.bits.sb_idx).addr := io.complete_info.bits.addr
        sb(io.complete_info.bits.sb_idx).data := io.complete_info.bits.data
    }

    // store port
    val lsuop   = sb(head).typ
    val st_addr = sb(head).addr
    val st_data = MuxLookup(lsuop, 0.U(XLEN.W))(Seq(
        LSUOpType.sb    -> (sb(head).data(7, 0) << st_addr(1, 0)),
        LSUOpType.sh    -> (sb(head).data(15, 0) << ((st_addr(1, 0) & "b10".U) << 3.U)),
        LSUOpType.sw    -> (sb(head).data)
    ))
    val st_size = MuxLookup(lsuop, 0.U(3.W))(Seq(
        LSUOpType.sb    -> "b000".U,
        LSUOpType.sh    -> "b001".U,
        LSUOpType.sw    -> "b010".U
    ))
    val st_mask = MuxLookup(lsuop, 0.U(MASKLEN.W))(Seq(
        LSUOpType.sb    -> ("b0001".U << st_addr(1, 0)),
        LSUOpType.sh    -> ("b0011".U << (st_addr(1, 0) & "b10".U)),
        LSUOpType.sw    -> "b1111".U
    ))

    io.st_port.req.valid    := sb(head).stat === SBstat.retired
    io.st_port.req.bits.wen := true.B
    io.st_port.req.bits.addr:= st_addr
    io.st_port.req.bits.data:= st_data
    io.st_port.req.bits.size:= st_size
    io.st_port.req.bits.mask:= st_mask

    io.st_port.resp.ready   := true.B

    when (io.st_port.req.fire){
        sb(head).stat := SBstat.invalid
        head := head + 1.U
    }

    // load bypass
    def align_addr(addr:UInt):UInt = {
        Cat(addr(XLEN - 1, 2), Fill(2, 0.B))
    }

    val hit_vec_o   = Wire(Vec(NR_SB, Bool()))
    for (i <- 0 until NR_SB){
        hit_vec_o(i) := align_addr(sb(i).addr) === align_addr(io.load_bypass.addr) & SBstat.is_complete(sb(i).stat)
    }
    val hit_vec     = GenLshiftedVec(hit_vec_o, head)
    val hit_idx     = Wire(UInt(SBbits.W))
    hit_idx := hit_vec.lastIndexWhere(_ === 1.B) + head

    io.load_bypass.hit  := hit_vec.contains(1.B)
    io.load_bypass.data := sb(hit_idx).data

}
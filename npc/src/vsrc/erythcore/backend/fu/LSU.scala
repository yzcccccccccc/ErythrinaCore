package erythcore.backend.fu

import chisel3._
import chisel3.util._
import erythcore._
import erythcore.common._
import bus.ivybus._
import utils._

object LSUOpType{
  def lb      = "b000".U
  def lh      = "b001".U
  def lw      = "b010".U
  def lbu     = "b011".U
  def lhu     = "b100".U

  def sb      = "b101".U
  def sh      = "b110".U
  def sw      = "b111".U

  def isStore(op: UInt) = op(2) & op(1, 0).orR
  def isLoad(op: UInt)  = ~isStore(op)

  def apply() = UInt(3.W)
}

class LSU extends Module with HasErythDefault{
  val io = IO(new Bundle {
    val in      = Flipped(Decoupled(new InstCtrlBlk))
    val out     = Decoupled(new InstCtrlBlk)

    val ld_port = new IvyBus

    // bypass the res from Store Buffer
    val bypass  = Flipped(new LdBpBundle)

    // complete Address Calculation
    val complete_info = Decoupled(new SBCompleteBundle)
  })

  val instblk = io.in.bits

  val need_to_store = LSUOpType.isStore(instblk.basicInfo.fuOpType) & instblk.basicInfo.instValid
  val need_to_load  = LSUOpType.isLoad(instblk.basicInfo.fuOpType) & instblk.basicInfo.instValid
  
  val addr    = instblk.basicInfo.imm + instblk.src1_dat

  // Bypass
  io.bypass.addr := addr
  val bp_hit  = io.bypass.hit & need_to_load

  // FSM
  val has_resp_fire = RegInit(false.B)
  when (io.ld_port.resp.fire & ~io.out.fire){
    has_resp_fire := true.B
  }.elsewhen(io.out.fire){
    has_resp_fire := false.B
  }

  val sIDLE :: sREQ :: sRECV :: Nil = Enum(3)
  val state = RegInit(sIDLE)
  switch(state){
    is (sIDLE){
      when (~reset.asBool){
        state := sREQ
      }
    }
    is (sREQ){
      when (io.ld_port.req.fire | bp_hit){
        state := sRECV
      }
    }
    is (sRECV){
      when ((io.ld_port.resp.fire | has_resp_fire | bp_hit) & io.out.fire){
        state := sREQ
      }
    }
  }

  // size
  val lsuop = instblk.basicInfo.fuOpType
  val size  = MuxLookup(lsuop, 0.U)(Seq(
    (lsuop === LSUOpType.lb)  -> "b000".U,
    (lsuop === LSUOpType.lbu) -> "b000".U,
    (lsuop === LSUOpType.lh)  -> "b001".U,
    (lsuop === LSUOpType.lhu) -> "b001".U,
    (lsuop === LSUOpType.lw)  -> "b010".U
  ))

  // res data
  val bp_data = RegEnable(io.bypass.data, state === sREQ & bp_hit)

  val rq_data_r = RegEnable(io.ld_port.resp.bits.data, io.ld_port.resp.fire)
  val rq_data   = Mux(has_resp_fire, rq_data_r, io.ld_port.resp.bits.data)

  val ld_data = Mux(bp_hit, bp_data, rq_data)

  val byteRes = MuxLookup(addr(1, 0), 0.U)(Seq(
    "b00".U -> ld_data(7, 0),
    "b01".U -> ld_data(15, 8),
    "b10".U -> ld_data(23, 16),
    "b11".U -> ld_data(31, 24)
  ))

  val hwordRes  = Mux(addr(1), ld_data(31, 16), ld_data(15, 0))
  val wordRes   = ld_data

  val loadRes = MuxLookup(lsuop, 0.U)(Seq(
    (lsuop === LSUOpType.lb)  -> SignExt(byteRes, XLEN),
    (lsuop === LSUOpType.lbu) -> ZeroExt(byteRes, XLEN),
    (lsuop === LSUOpType.lh)  -> SignExt(hwordRes, XLEN),
    (lsuop === LSUOpType.lhu) -> ZeroExt(hwordRes, XLEN),
    (lsuop === LSUOpType.lw)  -> wordRes
  ))

  // Complete Info (Store)
  val st_data = instblk.src2_dat
  io.complete_info.valid        := need_to_store
  io.complete_info.bits.sb_idx  := instblk.sb_idx
  io.complete_info.bits.addr    := addr
  io.complete_info.bits.data    := st_data

  // ld ports
  io.ld_port.req.valid      := need_to_load & state === sREQ & ~bp_hit
  io.ld_port.req.bits.addr  := addr
  io.ld_port.req.bits.wen   := false.B
  io.ld_port.req.bits.mask  := DontCare
  io.ld_port.req.bits.size  := size

  // Decoupled
  io.in.ready := io.out.ready & io.out.valid
  val ld_data_ok  = state === sRECV & (io.ld_port.resp.fire | has_resp_fire | bp_hit)
  io.out.valid  := ld_data_ok | ~instblk.basicInfo.instValid
}
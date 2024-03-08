import chisel3._
import chisel3.util._

class PS2_keyboard_iobundle(datawidth: Int) extends  Bundle{
    val ps2_clk     = Input(Bool())
    val ps2_data    = Input(Bool())
    val nextdata_n  = Input(Bool())
    val ready       = Output(Bool())
    val overflow    = Output(Bool())
    val data        = Output(UInt(datawidth.W))
}

class GenPS2_keyboard_ctrl(datawidth: Int) extends  Module{
    val io = IO(new PS2_keyboard_iobundle(datawidth))

    val buffer = Reg(UInt(10.W))
    val fifo = Reg(Vec(256, UInt(8.W)))
    val w_ptr = RegInit(0.U(3.W))
    val r_ptr = RegInit(0.U(3.W))
    val count = RegInit(0.U(4.W))
    val ps2_clk_sync = Reg(UInt(3.W))

    // Detect the neg edge
    ps2_clk_sync := Cat(ps2_clk_sync(1,0), io.ps2_clk)

    // sigal to triger sample
    val sampling = ps2_clk_sync(2) & ~ps2_clk_sync(1)

    val ready_r = RegInit(0.B)
    val overflow_r = RegInit(0.B)

    // receiver
    when (ready_r && ~io.nextdata_n){
        
    }

}
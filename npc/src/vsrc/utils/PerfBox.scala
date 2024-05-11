package utils

// Performance counter
import chisel3._
import chisel3.util.HasBlackBoxInline
import erythcore.ErythrinaSetting

class PerfIFU extends Bundle{
    val get_inst_event = Input(Bool())
}

class PerfIDU extends Bundle{
    val cal_inst_event = Input(Bool())
    val csr_inst_event = Input(Bool())
    val ld_inst_event = Input(Bool())       // Load
    val st_inst_event = Input(Bool())       // Store
    val j_inst_event = Input(Bool())    // Jump
    val b_inst_event = Input(Bool())    // Branch
}

class PerfEXU extends Bundle{
    // TODO: TBD
/*
    val alu_inst_event = Input(Bool())
    val mul_inst_event = Input(Bool())
    val div_inst_event = Input(Bool())
    val csr_inst_event = Input(Bool())
*/
}

class PerfMEMU extends Bundle{
    val ld_data_event = Input(Bool())
    val st_data_event = Input(Bool())
}

class PerfBoxIO extends Bundle{
    val inst_trigger = Input(Bool())
    val ifu_perf_probe = new PerfIFU
    val idu_perf_probe = new PerfIDU
    //val exu_probe = new PerfEXU
    val memu_perf_probe = new PerfMEMU
}

class PerfBox extends Module{
    val io = IO(new PerfBoxIO)

    if (!ErythrinaSetting.isSTA){
        // total cycles
        val total_cycles = RegInit(0.U(64.W))
        total_cycles := total_cycles + 1.U

        // total instructions
        val total_insts = RegInit(0.U(64.W))
        when(io.inst_trigger){
            total_insts := total_insts + 1.U
        }

        // total get instructions
        val total_get_insts = RegInit(0.U(64.W))
        when(io.ifu_perf_probe.get_inst_event){
            total_get_insts := total_get_insts + 1.U
        }

        // total cal instructions
        val total_cal_insts = RegInit(0.U(64.W))
        when(io.idu_perf_probe.cal_inst_event){
            total_cal_insts := total_cal_insts + 1.U
        }

        // total csr instructions
        val total_csr_insts = RegInit(0.U(64.W))
        when(io.idu_perf_probe.csr_inst_event){
            total_csr_insts := total_csr_insts + 1.U
        }

        // total load instructions
        val total_ld_insts = RegInit(0.U(64.W))
        when(io.idu_perf_probe.ld_inst_event){
            total_ld_insts := total_ld_insts + 1.U
        }

        // total store instructions
        val total_st_insts = RegInit(0.U(64.W))
        when(io.idu_perf_probe.st_inst_event){
            total_st_insts := total_st_insts + 1.U
        }

        // total jump instructions
        val total_j_insts = RegInit(0.U(64.W))
        when(io.idu_perf_probe.j_inst_event){
            total_j_insts := total_j_insts + 1.U
        }

        // total branch instructions
        val total_b_insts = RegInit(0.U(64.W))
        when(io.idu_perf_probe.b_inst_event){
            total_b_insts := total_b_insts + 1.U
        }

        // total load data events
        val total_ld_data_events = RegInit(0.U(64.W))
        when(io.memu_perf_probe.ld_data_event){
            total_ld_data_events := total_ld_data_events + 1.U
        }

        // total store data events
        val total_st_data_events = RegInit(0.U(64.W))
        when(io.memu_perf_probe.st_data_event){
            total_st_data_events := total_st_data_events + 1.U
        }

        // don't touch!!
        dontTouch(total_cycles)
        dontTouch(total_insts)
        dontTouch(total_get_insts)
        dontTouch(total_cal_insts)
        dontTouch(total_csr_insts)
        dontTouch(total_ld_insts)
        dontTouch(total_st_insts)
        dontTouch(total_j_insts)
        dontTouch(total_b_insts)
        dontTouch(total_ld_data_events)
        dontTouch(total_st_data_events)
    }
}
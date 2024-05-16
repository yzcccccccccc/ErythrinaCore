package utils

// Performance counter
import chisel3._
import chisel3.util.HasBlackBoxInline
import erythcore.ErythrinaSetting

class PerfIFU extends Bundle{
    val get_inst_event = Input(Bool())
    val wait_req_event = Input(Bool())      // Wait for request ready
    val wait_resp_event = Input(Bool())     // Wait for response valid
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

class PerfBPU extends Bundle{
    val hit_event   = Input(Bool())
    val miss_event  = Input(Bool())
}

class PerfMEMU extends Bundle{
    val ld_data_event = Input(Bool())
    val st_data_event = Input(Bool())
    val wait_req_event = Input(Bool())      // Wait for request ready
    val wait_resp_event = Input(Bool())     // Wait for response valid
}

class PerfBoxIO extends Bundle{
    val inst_trigger = Input(Bool())
    val ifu_perf_probe = new PerfIFU
    val idu_perf_probe = new PerfIDU
    //val exu_probe = new PerfEXU
    val memu_perf_probe = new PerfMEMU
    val bpu_perf_probe = new PerfBPU
}

class PerfBox extends Module{
    val io = IO(new PerfBoxIO)

    if (!ErythrinaSetting.isSTA){
        // total cycles
        val total_cycles = RegInit(0.U(64.W))
        dontTouch(total_cycles)
        total_cycles := total_cycles + 1.U

        // total instructions
        val total_insts = RegInit(0.U(64.W))
        dontTouch(total_insts)
        when(io.inst_trigger){
            total_insts := total_insts + 1.U
        }

        /*----------------Instruction Path Events----------------*/
        // total get instructions
        val total_get_insts = RegInit(0.U(64.W))
        dontTouch(total_get_insts)
        when(io.ifu_perf_probe.get_inst_event){
            total_get_insts := total_get_insts + 1.U
        }

        // total IFU wait request ready
        val total_ifu_wait_req = RegInit(0.U(64.W))
        dontTouch(total_ifu_wait_req)
        when(io.ifu_perf_probe.wait_req_event){
            total_ifu_wait_req := total_ifu_wait_req + 1.U
        }

        // total IFU wait response valid
        val total_ifu_wait_resp = RegInit(0.U(64.W))
        dontTouch(total_ifu_wait_resp)
        when(io.ifu_perf_probe.wait_resp_event){
            total_ifu_wait_resp := total_ifu_wait_resp + 1.U
        }

        /*----------------Calculate Events----------------*/
        // total cal instructions
        val total_cal_insts = RegInit(0.U(64.W))
        dontTouch(total_cal_insts)
        when(io.idu_perf_probe.cal_inst_event){
            total_cal_insts := total_cal_insts + 1.U
        }

        // total csr instructions
        val total_csr_insts = RegInit(0.U(64.W))
        dontTouch(total_csr_insts)
        when(io.idu_perf_probe.csr_inst_event){
            total_csr_insts := total_csr_insts + 1.U
        }

        // total load instructions
        val total_ld_insts = RegInit(0.U(64.W))
        dontTouch(total_ld_insts)
        when(io.idu_perf_probe.ld_inst_event){
            total_ld_insts := total_ld_insts + 1.U
        }

        // total store instructions
        val total_st_insts = RegInit(0.U(64.W))
        dontTouch(total_st_insts)
        when(io.idu_perf_probe.st_inst_event){
            total_st_insts := total_st_insts + 1.U
        }

        // total jump instructions
        val total_j_insts = RegInit(0.U(64.W))
        dontTouch(total_j_insts)
        when(io.idu_perf_probe.j_inst_event){
            total_j_insts := total_j_insts + 1.U
        }

        // total branch instructions
        val total_b_insts = RegInit(0.U(64.W))
        dontTouch(total_b_insts)
        when(io.idu_perf_probe.b_inst_event){
            total_b_insts := total_b_insts + 1.U
        }

        /*----------------Data Path Events----------------*/
        // total load data events
        val total_ld_data_events = RegInit(0.U(64.W))
        dontTouch(total_ld_data_events)
        when(io.memu_perf_probe.ld_data_event){
            total_ld_data_events := total_ld_data_events + 1.U
        }

        // total store data events
        val total_st_data_events = RegInit(0.U(64.W))
        dontTouch(total_st_data_events)
        when(io.memu_perf_probe.st_data_event){
            total_st_data_events := total_st_data_events + 1.U
        }

        // total MEMU wait request ready
        val total_memu_wait_req = RegInit(0.U(64.W))
        dontTouch(total_memu_wait_req)
        when(io.memu_perf_probe.wait_req_event){
            total_memu_wait_req := total_memu_wait_req + 1.U
        }

        // total MEMU wait response valid
        val total_memu_wait_resp = RegInit(0.U(64.W))
        dontTouch(total_memu_wait_resp)
        when(io.memu_perf_probe.wait_resp_event){
            total_memu_wait_resp := total_memu_wait_resp + 1.U
        }

        /*---------------- BPU event ----------------*/
        // total BPU hit event
        val total_bpu_hit = RegInit(0.U(64.W))
        dontTouch(total_bpu_hit)
        when(io.bpu_perf_probe.hit_event){
            total_bpu_hit := total_bpu_hit + 1.U
        }

        // total BPU miss event
        val total_bpu_miss = RegInit(0.U(64.W))
        dontTouch(total_bpu_miss)
        when(io.bpu_perf_probe.miss_event){
            total_bpu_miss := total_bpu_miss + 1.U
        }
    }
}
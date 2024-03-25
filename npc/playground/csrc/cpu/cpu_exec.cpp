
// for CPU simulations
#include "setting.h"
#include "memory.h"
#include "cpu.h"
#include "VErythrinaCore.h"
#include "verilated.h"
#include "verilated_vcd_c.h"
#include "svdpi.h"
#include "VErythrinaCore__Dpi.h"

void wave_record(VerilatedVcdC *tfp, VerilatedContext *contx){
    if (DUMP_WAVE){
        tfp->dump(contx->time());
    }
}

void single_cycle(VErythrinaCore *dut, VerilatedVcdC *tfp, VerilatedContext* contextp){
    dut->clock = !dut->clock;
    dut->eval();
    wave_record(tfp, contextp);
    contextp->timeInc(1);
    dut->clock = !dut->clock;
    dut->eval();
    wave_record(tfp, contextp);
    contextp->timeInc(1);
}

void half_cycle(VErythrinaCore *dut, VerilatedVcdC *tfp, VerilatedContext* contextp){
    dut->clock = !dut->clock;
    dut->eval();
    wave_record(tfp, contextp);
    contextp->timeInc(1);
}

int stop = 0;
extern "C" void halt_sim(){
    stop = 1;
    return;
}

void CPU_sim(){
    VerilatedContext *contx = new VerilatedContext;
    VErythrinaCore *dut = new VErythrinaCore(contx);
    VerilatedVcdC *tfp = (DUMP_WAVE) ? new VerilatedVcdC : NULL;

    if (DUMP_WAVE){
        contx->traceEverOn(true);
        dut->trace(tfp, 0); 
        tfp->open("wave.vcd");
    }

    // Reset
    dut->clock = 0;
    dut->reset = 1;
    for (int i = 0; i < 10; i++)    single_cycle(dut, tfp, contx);
    half_cycle(dut, tfp, contx);
    dut->reset = 0;
    half_cycle(dut, tfp, contx);

    // Simulate
    while (!stop){
        // Memory Request
        if (dut->io_MemReq_valid){
            if (dut->io_MemReq_bits_mask)       // write
                pmem_write(dut->io_MemReq_bits_addr, dut->io_MemReq_bits_data, dut->io_MemReq_bits_mask);
            else
                dut->io_MemResp_bits_data = pmem_read(dut->io_MemReq_bits_addr);
        }

        if (dut->io_InstCommit_rf_wen){
            printf("[Trace]: PC=0x%x, Inst=0x%x, rf_waddr=0x%x, rf_wdata=0x%x\n",
                dut->io_InstCommit_pc, dut->io_InstCommit_inst,
                dut->io_InstCommit_rf_waddr, dut->io_InstCommit_rf_wdata);
        }

        single_cycle(dut, tfp, contx);
    }

    // End
    delete dut;
    if (DUMP_WAVE){
        tfp->close();
    }
    delete contx;
}
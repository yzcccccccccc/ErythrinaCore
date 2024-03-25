
// for CPU simulations
#include "setting.h"
#include "memory.h"
#include "cpu.h"
#include "dpi.h"

#include "VSoc.h"
#include "verilated.h"
#include "verilated_vcd_c.h"


void wave_record(VerilatedVcdC *tfp, VerilatedContext *contx){
    if (DUMP_WAVE){
        tfp->dump(contx->time());
    }
}

void half_cycle(VSoc *dut, VerilatedVcdC *tfp, VerilatedContext* contextp){
    dut->clock = !dut->clock;
    dut->eval();
    wave_record(tfp, contextp);
    contextp->timeInc(1);
}

void single_cycle(VSoc *dut, VerilatedVcdC *tfp, VerilatedContext* contextp){
    half_cycle(dut, tfp, contextp);
    
    half_cycle(dut, tfp, contextp);
}

void CPU_sim(){
    VerilatedContext *contx = new VerilatedContext;
    VSoc *dut = new VSoc(contx);
    VerilatedVcdC *tfp = (DUMP_WAVE) ? new VerilatedVcdC : NULL;

    if (DUMP_WAVE){
        contx->traceEverOn(true);
        dut->trace(tfp, 1); 
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
        if (dut->io_commit_rf_wen){
            printf("[Trace]: PC=0x%08x, Inst=0x%08x, rf_waddr=0x%x, rf_wdata=0x%08x\n",
                dut->io_commit_pc, dut->io_commit_inst,
                dut->io_commit_rf_waddr, dut->io_commit_rf_wdata);
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
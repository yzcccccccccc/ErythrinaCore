#include <cstdint>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "VErythrinaCore.h"
#include "verilated.h"
#include "verilated_vcd_c.h"

#include "svdpi.h"
#include "VErythrinaCore__Dpi.h"
#define PCREST  0x80000000

int stop = 0;
extern "C" void halt(){
    stop = 1;
    return;
}

static const uint32_t inst[] = {
    0x00100093,     // addi x1, x0, 1
    0x00100113,     // addi x2, x0, 1
    0x00100073      // ebreak
};

void single_cycle(VErythrinaCore *dut, VerilatedVcdC *tfp, VerilatedContext* contextp){
    dut->clock = !dut->clock;
    dut->eval();
    tfp->dump(contextp->time());
    contextp->timeInc(1);
    dut->clock = !dut->clock;
    dut->eval();
    tfp->dump(contextp->time());
    contextp->timeInc(1);
}

void half_cycle(VErythrinaCore *dut, VerilatedVcdC *tfp, VerilatedContext* contextp){
    dut->clock = !dut->clock;
    dut->eval();
    tfp->dump(contextp->time());
    contextp->timeInc(1);
}

int main(int argc, char** argv) {
    VerilatedContext* contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    VErythrinaCore *dut = new VErythrinaCore(contextp);

    VerilatedVcdC* tfp = new VerilatedVcdC;
    contextp->traceEverOn(true);
    dut->trace(tfp, 0); 
    tfp->open("wave.vcd");

    // Reset
    dut->clock = 0;
    dut->reset = 1;
    for (int i = 0; i < 10; i++)
        single_cycle(dut, tfp, contextp);
    half_cycle(dut, tfp, contextp);
    dut->reset = 0;
    half_cycle(dut, tfp, contextp);

    // Work
    while (!stop){
        printf("Mem:0x%x (%d)\n", dut->io_MemReq_bits_addr, (dut->io_MemReq_bits_addr - PCREST)/4);
        dut->io_MemResp_bits_data = inst[(dut->io_MemReq_bits_addr - PCREST)/4];
        if (dut->io_InstCommit_rf_wen){
            printf("[Trace]: PC=0x%x, Inst=0x%x, rf_waddr=0x%x, rf_wdata=0x%x\n",
                dut->io_InstCommit_pc, dut->io_InstCommit_inst,
                dut->io_InstCommit_rf_waddr, dut->io_InstCommit_rf_wdata);
        }
        single_cycle(dut, tfp, contextp);
    }

    delete dut;
    tfp->close();
    delete contextp;
    return 0;
}
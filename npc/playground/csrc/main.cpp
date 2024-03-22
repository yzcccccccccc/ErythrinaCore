#include <cstdint>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "VTOP.h"
#include "verilated.h"
#include "verilated_vcd_c.h"

#define PCREST = 0x80000000

static const uint32_t inst[] = {
    0x00100093,     // addi x1, x0, 1
    0x00100113,     // addi x2, x0, 1
    0x00100073      // ebreak
};

int main(int argc, char** argv) {
    VerilatedContext* contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    VTOP* top = new VTOP{contextp};

    VerilatedVcdC* tfp = new VerilatedVcdC;
    contextp->traceEverOn(true);
    top->trace(tfp, 0); 
    tfp->open("wave.vcd");

    // Clk & rst
    top->clock = 1;
    top->reset = 0;

    // Work


    delete top;
    tfp->close();
    delete contextp;
    return 0;
}
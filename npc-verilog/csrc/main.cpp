#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "Vtop.h"

#ifdef VERILATOR_SIM
#include "verilated.h"
#include "verilated_vcd_c.h"
#endif

#ifdef NVBOARD
#include <nvboard.h>

static TOP_NAME dut;
void nvboard_bind_all_pins(TOP_NAME* top);

void single_cycle() {
  dut.clk = 0; dut.eval();
  dut.clk = 1; dut.eval();
}

void reset(int n) {
  dut.rst = 1;
  while (n -- > 0) single_cycle();
  dut.rst = 0;
}

#endif

int main(int argc, char** argv) {
#ifdef VERILATOR_SIM
    VerilatedContext* contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    Vtop* top = new Vtop{contextp};


    VerilatedVcdC* tfp = new VerilatedVcdC;
    contextp->traceEverOn(true);
    top->trace(tfp, 0); 
    tfp->open("wave.vcd");

    top->clk = 0;
    top->rst = 1;

    // Reset
    for (int i = 0; !contextp->gotFinish() && i < 10; i++){
        top->eval();
        top->clk = !top->clk;
        tfp->dump(contextp->time());
        contextp->timeInc(1);
    }
    top->rst = 0;

    // Work
    for (int i = 0; !contextp->gotFinish() && i < 100; i++){
        top->eval();
        top->clk = !top->clk;
        tfp->dump(contextp->time());
        contextp->timeInc(1);
    }
    delete top;
    tfp->close();
    delete contextp;
#endif

#ifdef NVBOARD
    nvboard_bind_all_pins(&dut);
    nvboard_init();
    reset(10);

    while(1) {
        nvboard_update();
        single_cycle();
    }
#endif
    return 0;
}
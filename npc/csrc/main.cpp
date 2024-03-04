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
#include <Vtop.h>

static TOP_NAME dut;
void nvboard_bind_all_pins(TOP_NAME* top);

static void single_cycle() {
  dut.a = rand() & 1;
  dut.b = rand() & 1;
  dut.eval();
  dut.a = rand() & 1;
  dut.b = rand() & 1;
  dut.eval();
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

    int i=20;
    while (!contextp->gotFinish() && i>=0) {
        int a = rand() & 1;
        int b = rand() & 1;
        top->a = a;
        top->b = b;
        top->eval();
        printf("a = %d, b = %d, f = %d\n", a, b, top->f);

        tfp->dump(contextp->time());
        contextp->timeInc(1);

        assert(top->f == (a^b));

        i--;
    }
    delete top;
    tfp->close();
    delete contextp;
#endif

#ifdef NVBOARD
    nvboard_bind_all_pins(&dut);
    nvboard_init();


    while(1) {
        nvboard_update();
        single_cycle();
    }
#endif
    return 0;
}
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "VTOP.h"

#ifdef VERILATOR_SIM
#include "verilated.h"
#include "verilated_vcd_c.h"
#endif

#ifdef NVBOARD
#include <nvboard.h>

static TOP_NAME dut;
void nvboard_bind_all_pins(TOP_NAME* top);

void single_cycle() {
    dut.eval();
}

#endif

int main(int argc, char** argv) {
#ifdef VERILATOR_SIM
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
    for (int i = 0; !contextp->gotFinish() && i < 100; i++){
        int res, x0, x1, x2, x3, y;
        x0 = 0b00;
        x1 = 0b01;
        x2 = 0b10;
        x3 = 0b11;
        y = rand() & 0b11;
        switch (y){
            case 0b00:
                res = x0;
                break;
            case 0b01:
                res = x1;
                break;
            case 0b10:
                res = x2;
                break;
            case 0b11:
                res = x3;
                break;
            default:;
        }
        top->io_X0 = x0;
        top->io_X1 = x1;
        top->io_X2 = x2;
        top->io_X3 = x3;
        top->io_Y = y;
        top->eval();
        tfp->dump(contextp->time());
        assert(top->io_F == res);
        printf("[SIM] io_Y = %d, res: io_F = %d\n", y, top->io_F);
        contextp->timeInc(1);
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
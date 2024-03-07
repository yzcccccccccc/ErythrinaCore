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
        int input = rand() & 0b11111111;
        int output = 0, en = (input > 0);
        for (int tmp = input;tmp > 0; tmp >>= 1){
            output++;
        }
        top->io_inputs = input;
        top->eval();
        tfp->dump(contextp->time());
        printf("[SIM] Input: %d, out: %d, valid: %d, seg: %x\n", input, top->io_outputs, top->io_valid, top->io_seg_res);
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
// for CPU simulations
#include "common.h"
#include "difftest.h"
#include "isa.h"
#include "setting.h"
#include "memory.h"
#include "cpu.h"
#include "dpi.h"
#include "util.h"
#include "device.h"

#include "VSoc.h"
#include "verilated.h"
#include "verilated_vcd_c.h"
#include <cstdint>

int cycle = 0;
NPC_state npc_state;

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

    cycle++;
    if (cycle > (uint32_t)CYCLE_BOUND){
        npc_state = CPU_ABORT_CYCLE_BOUND;
    }
}

// Soc DUT
VSoc *dut = NULL;
VerilatedVcdC *tfp = NULL;
VerilatedContext *contx = NULL;

void init_verilate(){
    contx = new VerilatedContext;
    dut = new VSoc(contx);
    tfp = (DUMP_WAVE) ? new VerilatedVcdC : NULL;

    if (DUMP_WAVE){
        contx->traceEverOn(true);
        dut->trace(tfp, 1); 
        tfp->open("wave.vcd");
    }
}

void CPU_reset(){
    // NPC_STATE
    npc_state = CPU_RUN;

    // Reset
    dut->clock = 0;
    dut->reset = 1;
    for (int i = 0; i < 10; i++)    single_cycle(dut, tfp, contx);
    half_cycle(dut, tfp, contx);
    dut->reset = 0;
    half_cycle(dut, tfp, contx);
}

// Report the end state
void report(){
    switch (npc_state) {
        case CPU_HALT_GOOD:
            printf("[Hit Trap] Halt from ebreak. Hit %sGood%s Trap\n", FontGreen, Restore);
            break;
        case CPU_HALT_BAD:
            printf("[Hit Trap] Halt from ebreak. Hit %sBad%s Trap\n", FontRed, Restore);
            break;
        case CPU_ABORT_MEMLEAK:
            printf("[Hit Trap] %sAbort%s from memory leak.\n", FontRed, Restore);
            break;
        case CPU_ABORT_INSTR_BOUND:
            printf("[Hit Trap] %sAbort%s from hitting instructions bound.\n", FontRed, Restore);
            break;
        case CPU_ABORT_CYCLE_BOUND:
            printf("[Hit Trap] %sAbort%s from hitting cycles bound.\n", FontRed, Restore);
            break;
        case CPU_ABORT_DIFF_ERR:
            printf("[Hit Trap] %sAbort%s from difftesting fail.\n", FontRed, Restore);
            break;
        default:
            printf("[Hit Trap] Unknown signal.\n");
    }
}

void collect(){
    delete dut;
    if (DUMP_WAVE){
        tfp->close();
    }

    if (npc_state != CPU_HALT_GOOD)
        assert(0);
    delete contx;
}

char inst_disasm[100];
void execute(uint32_t n){
    for (;n > 0 && npc_state == CPU_RUN; n--){
        while (!dut->io_commit_valid && npc_state == CPU_RUN) single_cycle(dut, tfp, contx);

        //void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
        if (ITRACE){
            disassemble(inst_disasm, 100, dut->io_commit_pc, (uint8_t *)&(dut->io_commit_inst), 4);
            printf("[Trace]: PC=0x%08x, Inst=0x%08x (%s), rf_waddr=0x%x, rf_wdata=0x%08x, rf_wen=%d\n\n",
                    dut->io_commit_pc, dut->io_commit_inst, inst_disasm,
                    dut->io_commit_rf_waddr, dut->io_commit_rf_wdata,
                    dut->io_commit_rf_wen);
        }

        single_cycle(dut, tfp, contx);
        update_npcstate();
        //if (dut->io_commit_rf_wen)
        //    CPU_state.gpr[dut->io_commit_rf_waddr] = dut->io_commit_rf_wdata;
        difftest_step(CPU_state.pc);
        single_cycle(dut, tfp, contx);
    }
    if (npc_state != CPU_RUN){
        report();
        collect();
    }
}

void init_CPU(){
    init_verilate();

    init_device();

    CPU_reset();
}

void CPU_sim(){
    // init
    init_CPU();

    // Simulate
    execute(-1);
}
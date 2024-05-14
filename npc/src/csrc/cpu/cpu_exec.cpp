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
#include "perf.h"

#include "verilated.h"
#include "verilated_vcd_c.h"
#include <cstdint>
#include <cstdio>

#ifdef __SOC__
#include "VysyxSoCFull.h"
#endif

#ifdef __SIM__
#include "VSimTop.h"
#endif

#ifdef NVBOARD
#include <nvboard.h>
void nvboard_bind_all_pins(VSoc* dut);

#endif

int cycle = 0;
FILE *logfile, *flash_log, *diff_log, *perf_log;
// NPC state
NPC_state npc_state;
uint32_t npc_info;

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
#ifdef NVBOARD
    nvboard_update();
#endif

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

void cpu_reset(){
    // NPC_STATE
    npc_state = CPU_RUN;

    // Reset
    dut->clock = 0;
    dut->reset = 1;
    for (int i = 0; i < 20; i++)    single_cycle(dut, tfp, contx);
    half_cycle(dut, tfp, contx);
    dut->reset = 0;
    half_cycle(dut, tfp, contx);
}

// Report the end state
void report(){
    switch (npc_state) {
        case CPU_HALT_GOOD:
            printf("[Hit Trap] Halt from ebreak. Hit %sGood%s Trap\n", FontGreen, Restore);
            perf_res_show();
            perf_res_record();
            break;
        case CPU_HALT_BAD:
            printf("[Hit Trap] Halt from ebreak. Hit %sBad%s Trap\n", FontRed, Restore);
            break;
        case CPU_ABORT_INSTERR:
            printf("[Hit Trap] %sAbort%s due to unknown instuctions.\n", FontRed, Restore);
            break;
        case CPU_ABORT_MEMLEAK:
            printf("[Hit Trap] %sAbort%s from memory leak at 0x%08x.\n", FontRed, Restore, npc_info);
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

void cpu_end(){
    if (ITRACE){
        fclose(logfile);
    }
    report();
    collect();
}

char inst_disasm[100];
void execute(uint32_t n){
    for (;n > 0 && npc_state == CPU_RUN && !contx->gotFinish(); n--){
        while (!get_commit_valid(dut) && npc_state == CPU_RUN) single_cycle(dut, tfp, contx);
        if (npc_state != CPU_RUN) break;
        //void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
        if (ITRACE){
            uint32_t inst   = get_commit_inst(dut);
            uint32_t pc     = get_commit_pc(dut);
            disassemble(inst_disasm, 100, pc, (uint8_t *)&(inst), 4);
            fprintf(logfile, "[Trace]: PC=0x%08x, Inst=0x%08x (%s), rf_waddr=0x%x, rf_wdata=0x%08x, rf_wen=%d, addr=0x%08x, en=%x\n",
                    pc, inst, inst_disasm,
                    get_commit_rf_waddr(dut), get_commit_rf_wdata(dut),
                    get_commit_rf_wen(dut),
                    get_commit_mem_addr(dut), get_commit_mem_en(dut));
        }
        if (MTRACE){
            uint32_t en = get_commit_mem_en(dut);
            uint32_t addr = get_commit_mem_addr(dut);
            if (en){
                fprintf(flash_log, "[mtrace] r/w at 0x%08x\n", addr);
            }
        }

        check_skip();

        single_cycle(dut, tfp, contx);
        update_npcstate();

        difftest_step(CPU_state.pc);
        single_cycle(dut, tfp, contx);
    }
    if (contx->gotFinish()){
        npc_state = CPU_HALT_BAD;
    }
    if (npc_state != CPU_RUN){
        cpu_end();
    }
}

void init_nvboard(){
#ifdef NVBOARD
    nvboard_bind_all_pins(dut);
    nvboard_init();
    
    printf("%s[INFO]%s NVBoard initialized.\n", FontGreen, Restore);
#endif
}

void init_cpu(){
    init_verilate();

    //init_mem();

    init_nvboard();

    cpu_reset();

    if (ITRACE){
        logfile = fopen("./build/itrace.log", "w");
    }
    if (MTRACE){
        flash_log = fopen("./build/mtrace.log", "w");
    }
    if (DIFF_TEST){
        diff_log = fopen("./build/diff.log", "w");
    }
    perf_log = fopen("./build/report/perf.log", "w");
}

void cpu_sim(){
    // init
    init_cpu();

    // Simulate
    execute(-1);
}
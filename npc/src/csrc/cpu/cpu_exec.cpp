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
#include "trace.h"

#include "verilated.h"
#include "verilated_fst_c.h"

#include <cstdint>
#include <cstdio>
#include <csignal>
#include <chrono>

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

uint64_t cycle = 0, instr = 0;
FILE *diff_log, *perf_log;
// NPC state
NPC_state npc_state;
uint32_t npc_info;

bool trace_is_on = DUMP_WAVE & !USE_WINDOW;

// Soc DUT
VSoc *dut = NULL;
VerilatedFstC *tfp = NULL;
VerilatedContext *contx = NULL;

void init_wave_dmp(){
    if (trace_is_on){
        printf("[INFO] Wave dump %senabled%s at cycle %ld.\n", FontGreen, Restore, cycle);
        tfp = new VerilatedFstC;
        contx->traceEverOn(true);
        dut->trace(tfp, 1);
        tfp->open("wave.vcd");
    }
}

void init_verilate(){
    contx = new VerilatedContext;
    dut = new VSoc(contx);
    init_wave_dmp();
}

void wave_record(VerilatedFstC *tfp, VerilatedContext *contx){
    if (trace_is_on){
        tfp->dump(contx->time());
    }
}

void half_cycle(VSoc *dut, VerilatedFstC *tfp, VerilatedContext* contextp){
    dut->clock = !dut->clock;
    dut->eval();
    wave_record(tfp, contextp);
    contextp->timeInc(1);
}

void single_cycle(VSoc *dut, VerilatedFstC *tfp, VerilatedContext* contextp){
#ifdef NVBOARD
    nvboard_update();
#endif
    half_cycle(dut, tfp, contextp);
    
    half_cycle(dut, tfp, contextp);

    cycle++;
    if (cycle > (uint64_t)CYCLE_BOUND){
        npc_state = CPU_ABORT_CYCLE_BOUND;
    }

    if (!trace_is_on && DUMP_WAVE && cycle > (uint64_t)WINDOW_BEGIN){
        trace_is_on = true;
        init_wave_dmp();
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
            perf_res_show();
            perf_res_record();
            printf("[Hit Trap] Halt from ebreak. Hit %sGood%s Trap\n", FontGreen, Restore);
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
        case CPU_ABORT_INTERRUPT:
            printf("[Hit Trap] %sAbort%s from interrupt.\n", FontRed, Restore);
            break;
        case CPU_ABORT_DIFF_ERR:
            printf("[Hit Trap] %sAbort%s from difftesting fail.\n", FontRed, Restore);
            break;
        case CPU_ABORT_TIMEOUT:
            printf("[Hit Trap] %sAbort%s from timeout.\n", FontRed, Restore);
            break;
        default:
            printf("[Hit Trap] %sAbort%s due to Unknown signal.\n", FontRed, Restore);
    }
}

void collect(){
    delete dut;
    if (DUMP_WAVE){
        if (!trace_is_on){
            printf("[%sWarning%s] Simulation cycles are not in the window, no wave file generated.\n", FontYellow, Restore);
        }
        else{
            tfp->close();
        }
    }
    delete contx;
    
    if (npc_state != CPU_HALT_GOOD)
        assert(0);
}

void cpu_end(){
    if (ITRACE){
        irbuf_dump();
        fclose(itrace_file);
    }
    if (DIFF_TEST){
        fclose(diff_log);
    }
    if (MTRACE){
        fclose(mtrace_file);
    }
    report();
    collect();
}

void execute(uint32_t n){
    auto start = std::chrono::high_resolution_clock::now();

    for (;n > 0 && npc_state == CPU_RUN && !contx->gotFinish(); n--){
        uint64_t cycle_start = cycle;
        while (!get_commit_valid(dut) && npc_state == CPU_RUN){
            single_cycle(dut, tfp, contx);
            if (cycle - cycle_start > TIMEOUT_BOUND){
                npc_state = CPU_ABORT_TIMEOUT;
                break;
            }
        }
        if (npc_state != CPU_RUN) break;
        instr++;
        if (ITRACE){
            itrace_record();
        }
        if (MTRACE){
            mtrace_record();
        }

        check_skip();
        update_npcstate();

        difftest_step(CPU_state.pc);
        single_cycle(dut, tfp, contx);
    }
    auto end = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> duration = end - start;

    printf("[Info] Total Cycles: %ld, Simulation Speed: %.2lf CPS\n", cycle, (double)cycle / duration.count());
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

void handle_interrupt(int signum){
    if (signum == SIGINT){
        npc_state = CPU_ABORT_INTERRUPT;
    }
}

void init_cpu(){
    signal(SIGINT, handle_interrupt);

    init_verilate();

    //init_mem();

    init_nvboard();

    cpu_reset();

    if (ITRACE){
        itrace_init();
    }
    if (MTRACE){
        mtrace_init();
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
#ifndef _CPU_H__
#define _CPU_H__

#include "setting.h"

#ifdef __SOC__
#include "VysyxSoCFull.h"
#endif

#ifdef __SIM__
#include "VSimTop.h"
#endif

#include <cstdint>

typedef enum{
    CPU_RUN,
    CPU_ABORT_MEMLEAK,
    CPU_ABORT_INSTERR,
    CPU_ABORT_INSTR_BOUND,
    CPU_ABORT_CYCLE_BOUND,
    CPU_ABORT_DIFF_ERR,
    CPU_ABORT_INTERRUPT,
    CPU_ABORT_TIMEOUT,          // no response from DUT
    CPU_HALT_GOOD,
    CPU_HALT_BAD
}NPC_state;
extern NPC_state npc_state;
extern uint32_t npc_info;

#define TIMEOUT_BOUND 1000000

extern void cpu_sim();
extern void cpu_end();
extern void init_cpu();
extern void execute(uint32_t n);

// dut soc
#ifdef __SOC__
typedef VysyxSoCFull VSoc;
#endif
#ifdef __SIM__
typedef VSimTop VSoc;
#endif
extern VSoc *dut;
extern VerilatedFstC *tfp;
extern VerilatedContext *contx;
extern bool trace_is_on;
extern void init_wave_dmp();

extern void half_cycle(VSoc *dut, VerilatedFstC *tfp, VerilatedContext* contextp);
extern void single_cycle(VSoc *dut, VerilatedFstC *tfp, VerilatedContext* contextp);
extern uint64_t cycle;

extern FILE  *diff_log, *perf_log;
extern uint64_t cycle, instr;

// commit tools
extern uint32_t get_commit_valid(VSoc *dut);
extern uint32_t get_commit_pc(VSoc *dut);
extern uint32_t get_commit_inst(VSoc *dut);
extern uint32_t get_commit_rf_waddr(VSoc *dut);
extern uint32_t get_commit_rf_wdata(VSoc *dut);
extern uint32_t get_commit_rf_wen(VSoc *dut);
extern uint32_t get_commit_mem_addr(VSoc *dut);
extern uint32_t get_commit_mem_en(VSoc *dut);
extern uint32_t get_commit_mem_data(VSoc *dut);
extern uint32_t get_commit_mem_wen(VSoc *dut);

#endif
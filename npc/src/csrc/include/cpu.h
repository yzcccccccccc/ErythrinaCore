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
    CPU_HALT_GOOD,
    CPU_HALT_BAD
}NPC_state;
extern NPC_state npc_state;
extern uint32_t npc_info;

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

extern FILE *logfile, *flash_log, *diff_log, *perf_log;

// commit tools
extern uint32_t get_commit_valid(VSoc *dut);
extern uint32_t get_commit_pc(VSoc *dut);
extern uint32_t get_commit_inst(VSoc *dut);
extern uint32_t get_commit_rf_waddr(VSoc *dut);
extern uint32_t get_commit_rf_wdata(VSoc *dut);
extern uint32_t get_commit_rf_wen(VSoc *dut);
extern uint32_t get_commit_mem_addr(VSoc *dut);
extern uint32_t get_commit_mem_en(VSoc *dut);

#endif
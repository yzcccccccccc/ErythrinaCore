#ifndef _CPU_H__
#define _CPU_H__

#include "VysyxSoCFull.h"
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

extern void CPU_sim();
extern void init_CPU();
extern void execute(uint32_t n);

// dut soc
typedef VysyxSoCFull VSoc;
extern VSoc *dut;

// commit tools
extern uint32_t get_commit_valid(VSoc *dut);
extern uint32_t get_commit_pc(VSoc *dut);
extern uint32_t get_commit_inst(VSoc *dut);
extern uint32_t get_commit_rf_waddr(VSoc *dut);
extern uint32_t get_commit_rf_wdata(VSoc *dut);
extern uint32_t get_commit_rf_wen(VSoc *dut);

#endif
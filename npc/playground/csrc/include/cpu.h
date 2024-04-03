#ifndef _CPU_H__
#define _CPU_H__

#include "VSoc.h"

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
extern uint32_t npc_val;

extern void CPU_sim();
extern void init_CPU();
extern void execute(uint32_t n);

// dut soc
extern VSoc *dut;

#endif
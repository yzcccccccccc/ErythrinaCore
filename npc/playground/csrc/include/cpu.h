#ifndef _CPU_H__
#define _CPU_H__

#include "VSoc.h"

typedef enum{
    CPU_RUN,
    CPU_ABORT_MEMLEAK,
    CPU_ABORT_INSTR_BOUND,
    CPU_ABORT_CYCLE_BOUND,
    CPU_HALT_GOOD,
    CPU_HALT_BAD
}CPU_state;
extern CPU_state npc_state;

extern void CPU_sim();
extern void init_CPU();
extern void execute(uint32_t n);

// dut soc
extern VSoc *dut;

#endif
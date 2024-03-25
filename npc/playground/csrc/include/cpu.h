#ifndef _CPU_H__
#define _CPU_H__

typedef enum{
    CPU_RUN,
    CPU_ABORT_MEMLEAK,
    CPU_ABORT_INSTR_BOUND,
    CPU_HALT
}CPU_state;
extern CPU_state stop;
extern void CPU_sim();

#endif
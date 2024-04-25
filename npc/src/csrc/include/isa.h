#ifndef __ISA_H__
#define __ISA_H__

#include "common.h"
#define REG_NUM 32

typedef struct{
    uint32_t gpr[REG_NUM];
    uint32_t pc;
}rv32_CPU_state;

extern rv32_CPU_state CPU_state;
extern const char *regs[];

extern void update_npcstate();
extern uint32_t RTL_REGFILE(int index);
extern uint32_t RTL_PC();
extern char *get_regname(int index);

#endif
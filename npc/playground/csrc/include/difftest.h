#ifndef __DIFFTEST_H__
#define __DIFFTEST_H__

#include "memory.h"

void init_difftest(char *ref_so_file, long img_size, int port);

extern void (*ref_difftest_memcpy)(paddr_t addr, void *buf, int n, bool direction);
extern void (*ref_difftest_regcpy)(void *dut, bool direction);
extern void (*ref_difftest_exec)(uint32_t n);
extern void (*ref_difftest_init)(int port);

extern void difftest_step(uint32_t pc);

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

extern bool is_skip;

#endif
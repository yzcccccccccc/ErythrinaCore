#include "difftest.h"
#include "common.h"
#include "cpu.h"
#include "isa.h"
#include <cstdio>
#include <dlfcn.h>
#include <cassert>
#include <cstddef>
#include <memory.h>

void (*ref_difftest_memcpy)(paddr_t addr, void *buf, int n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint32_t n) = NULL;
void (*ref_difftest_init)(int port) = NULL;

void init_difftest(char *ref_so_file, long img_size, int port) {
    assert(ref_so_file != NULL);

    void *handle;
    handle = dlopen(ref_so_file, RTLD_LAZY);
    assert(handle);

    ref_difftest_memcpy = (void (*)(paddr_t, void *, int, bool))dlsym(handle, "difftest_memcpy");
    assert(ref_difftest_memcpy);

    ref_difftest_regcpy = (void (*)(void *, bool))dlsym(handle, "difftest_regcpy");
    assert(ref_difftest_regcpy);

    ref_difftest_exec = (void (*)(uint32_t n))dlsym(handle, "difftest_exec");
    assert(ref_difftest_exec);

    void (*ref_difftest_init)(int) = (void (*)(int))dlsym(handle, "difftest_init");
    assert(ref_difftest_init);

    ref_difftest_init(port);
    ref_difftest_memcpy(PC_RSTVEC, guest2host(PC_RSTVEC), img_size, DIFFTEST_TO_REF);
    ref_difftest_regcpy(&CPU_state, DIFFTEST_TO_REF);
}

bool checkregs(rv32_CPU_state *ref, uint32_t pc){
    if (ref->pc != CPU_state.pc){
        printf("[difftest] NPC error, ref_r pc: 0x%08x, nemu pc: 0x%08x\n", ref->pc, CPU_state.pc);
        return false;
    }
    for (int i = 0; i < REG_NUM; i++){
        if (ref->gpr[i] != CPU_state.gpr[i]){
            printf("[difftest] At pc 0x%08x, Reg %s error, ref: 0x%08x, npc: 0x%08x\n", pc, get_regname(i), ref->gpr[i], CPU_state.gpr[i]);
            return false;
        }
    }
  return true;

}

void difftest_step(uint32_t pc){
    rv32_CPU_state ref_r;
    ref_difftest_exec(1);
    ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);
    if (!checkregs(&ref_r, pc))
        npc_state = CPU_ABORT_DIFF_ERR;
}
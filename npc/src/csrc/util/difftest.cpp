#include "difftest.h"
#include "setting.h"
#include "common.h"
#include "cpu.h"
#include "isa.h"
#include <cstdio>
#include <dlfcn.h>
#include <cassert>
#include <cstddef>
#include <memory.h>
#include <device.h>

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

    if (DIFF_TEST){
        ref_difftest_init(port);
        ref_difftest_memcpy(PC_RSTVEC, guest2host(PC_RSTVEC), img_size, DIFFTEST_TO_REF);
        ref_difftest_memcpy(FLASH_BASE, guest2host(FLASH_BASE), FLASH_SIZE, DIFFTEST_TO_REF);
    }
}

bool checkregs(rv32_CPU_state *ref, uint32_t pc){
    if (ref->pc != CPU_state.pc){
        printf("[difftest] NPC error, NEMU pc: 0x%08x, NPC pc: 0x%08x\n", ref->pc, CPU_state.pc);
        return false;
    }
    for (int i = 0; i < REG_NUM; i++){
        if (ref->gpr[i] != CPU_state.gpr[i]){
            printf("[difftest] At pc 0x%08x, Reg %s error, NEMU: 0x%08x, NPC: 0x%08x\n", pc, get_regname(i), ref->gpr[i], CPU_state.gpr[i]);
            return false;
        }
    }
  return true;

}

bool is_skip = 0;

void check_skip(){
    uint32_t addr   = get_commit_mem_addr(dut);
    uint32_t en     = get_commit_mem_en(dut);
    if (addr >= DEV_CLINT && addr < DEV_CLINT + DEV_CLINT_SZ && en){
        if (DIFF_TEST)
            fprintf(diff_log, "[difftest] Skip clint (addr=0x%08x)\n", addr);
        is_skip = 1;
    }
    else if (addr >= DEV_UART && addr < DEV_UART + DEV_UART_SZ && en){
        if (DIFF_TEST)
            fprintf(diff_log, "[difftest] Skip uart (addr=0x%08x)\n", addr);
        is_skip = 1;
    }
    else if (addr >= DEV_SPI && addr < DEV_SPI + DEV_SPI_SZ && en){
        if (DIFF_TEST)
            fprintf(diff_log, "[difftest] Skip spi (addr=0x%08x)\n", addr);
        is_skip = 1;
    }
    else{
        is_skip = 0;
    }
}

void difftest_step(uint32_t pc){
    if (DIFF_TEST){
        if (!is_skip){
            fprintf(diff_log, "[difftest] Run this cycle (pc=0x%08x)\n", pc);
            rv32_CPU_state ref_r;
            ref_difftest_exec(1);
            ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);
            if (!checkregs(&ref_r, pc))
                npc_state = CPU_ABORT_DIFF_ERR;
        }
        else{
            fprintf(diff_log, "[difftest] Skip this cycle (pc=0x%08x)\n", pc);
            ref_difftest_regcpy(&CPU_state, DIFFTEST_TO_REF);
        }
    }
}
#include "cpu.h"
#include "setting.h"
#include "svdpi.h"
#include "VysyxSoCFull__Dpi.h"
#include "VysyxSoCFull___024root.h"

#include "dpi.h"
#include "memory.h"
#include <cstdio>

extern "C" void flash_read(int addr, int *data) {
    *data = *(uint32_t *)(flash + (addr & (~0x3u)));
    if (MTRACE)
        fprintf(flash_log, "[flash ]read at 0x%x (data: 0x%08x)\n", addr & (~0x3u), *data);
}
extern "C" void mrom_read(int addr, int *data) {
    *data = pmem_read(addr & (~0x3u));
}

extern "C" void halt_Ebreak(){
    if (dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_10)    // a0 == 1
        npc_state = CPU_HALT_BAD;
    else
        npc_state = CPU_HALT_GOOD;
    return;
}

extern "C" void halt_UnknownINST(){
    if (npc_state == CPU_RUN){
        printf("[Halt NINST] unknown inst at 0x%08x\n", dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__IFU_inst__DOT__pc);
        npc_state = CPU_ABORT_INSTERR;
    }
    return;
}

extern "C" int mem_read(int paddr){
    return pmem_read(paddr & (~0x3u));
}

extern "C" void mem_write(int paddr, const svBitVecVal *mask, int data){
    pmem_write(paddr & (~0x3u), data, *mask);
}
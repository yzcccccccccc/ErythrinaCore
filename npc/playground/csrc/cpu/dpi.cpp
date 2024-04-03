#include "cpu.h"
#include "svdpi.h"
#include "VSoc__Dpi.h"
#include "VSoc___024root.h"

#include "dpi.h"
#include "memory.h"
#include <cstdio>

extern "C" void halt_Ebreak(){
    if (dut->rootp->Soc__DOT__erythrinacore__DOT__regfile__DOT__RegArray_10)    // a0 == 1
        npc_state = CPU_HALT_BAD;
    else
        npc_state = CPU_HALT_GOOD;
    return;
}

extern "C" void halt_UnknownINST(){
    if (npc_state == CPU_RUN){
        printf("[Halt NINST] unknown inst at 0x%08x\n", dut->rootp->Soc__DOT__erythrinacore__DOT__IFU_inst__DOT__pc);
        npc_state = CPU_ABORT_INSTERR;
    }
    return;
}

extern "C" int mem_read(int paddr){
    return pmem_read(paddr);
}

extern "C" void mem_write(int paddr, const svBitVecVal *mask, int data){
    pmem_write(paddr, data, *mask);
}
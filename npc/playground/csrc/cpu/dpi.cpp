#include "cpu.h"
#include "svdpi.h"
#include "VSoc__Dpi.h"
#include "VSoc___024root.h"

#include "dpi.h"
#include "memory.h"
#include <cstdio>

extern "C" void halt_sim(){
    if (dut->rootp->Soc__DOT__erythrinacore__DOT__regfile__DOT__RegArray_10)    // a0 == 1
        npc_state = CPU_HALT_BAD;
    else
        npc_state = CPU_HALT_GOOD;
    return;
}

extern "C" int mem_read(int paddr){
    return pmem_read(paddr);
}

extern "C" void mem_write(int paddr, const svBitVecVal *mask, int data){
    pmem_write(paddr, data, *mask);
}
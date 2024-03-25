#include "svdpi.h"
#include "VSoc__Dpi.h"

#include "dpi.h"
#include "memory.h"
#include <cstdio>

int stop = 0;
extern "C" void halt_sim(){
    stop = 1;
    return;
}

extern "C" int mem_read(int paddr){
    printf("MemRead 0x%08x\n", paddr);
    return pmem_read(paddr);
}

extern "C" void mem_write(int paddr, const svBitVecVal *mask, int data){
    pmem_write(paddr, data, *mask);
}
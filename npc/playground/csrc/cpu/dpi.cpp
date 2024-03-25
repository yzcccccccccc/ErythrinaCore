#include "cpu.h"
#include "svdpi.h"
#include "VSoc__Dpi.h"

#include "dpi.h"
#include "memory.h"
#include <cstdio>

extern "C" void halt_sim(){
    stop = CPU_HALT;
    return;
}

extern "C" int mem_read(int paddr){
    return pmem_read(paddr);
}

extern "C" void mem_write(int paddr, const svBitVecVal *mask, int data){
    pmem_write(paddr, data, *mask);
}
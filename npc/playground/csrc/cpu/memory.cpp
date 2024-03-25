#include "common.h"
#include "memory.h"
#include <cassert>
#include <cstdio>
#include <cstdint>

// TODO: only for simulation...
static uint8_t pmem[MEMSIZE];

uint8_t* guest2host(paddr_t paddr){
    return pmem + paddr - MEMBASE;
}

uint32_t host_read(void *addr){
    return *(uint32_t *)addr;
}

uint32_t host_write(void *addr, uint32_t data, uint32_t mask){
    uint32_t real_data = 0;
    for (int i = 0; i < 4; i++){
        if (mask & 1){
            real_data |= (data & 0xff);
        }
        mask >>= 1;
        data >>= 8;
    }
    *(uint32_t *)addr = real_data;
    return real_data;
}

uint32_t pmem_read(paddr_t addr){
    uint32_t host_index = addr - MEMBASE;
    assert(host_index + 3 < MEMSIZE);
    uint32_t res = host_read(guest2host(addr));
    //printf("[Trace]: MemRead at 0x%08x, res: 0x%08x\n", addr, res);
    return res;
}

uint32_t pmem_write(paddr_t addr, uint32_t data, uint32_t mask){
    uint32_t host_index = addr - MEMBASE;
    assert(host_index + 3 < MEMSIZE);
    //printf("[Trace]: MemWrite at 0x%08x\n", addr);
    return host_write(guest2host(addr), data, mask);
}
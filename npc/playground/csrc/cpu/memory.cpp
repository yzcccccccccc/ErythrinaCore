#include "common.h"
#include "memory.h"
#include "dpi.h"
#include <cassert>
#include <cstdio>
#include <cstdint>

uint8_t* guest2host(paddr_t paddr){
    return pmem + paddr - MEMBASE;
}

uint32_t host_read(void *addr){
    return *(uint32_t *)addr;
}

uint32_t host_write(void *addr, uint32_t data, uint32_t mask){
    uint32_t real_mask = 0;
    for (int i = 0; i < 4; i++){
        if (mask & 1){
            real_mask |= 0xff << (i * 8);
        }
        mask >>= 1;
    }
    printf("%08x\n", real_mask);
    uint32_t real_data = *(uint32_t *)addr & (~real_mask) | data & real_mask;
    *(uint32_t *)addr = real_data;
    return real_data;
}

uint32_t pmem_read(paddr_t addr){
    uint32_t host_index = addr - MEMBASE;
    npc_alert(host_index + 3 < MEMSIZE);
    assert(host_index + 3 < MEMSIZE);
    uint32_t res = host_read(guest2host(addr));
    printf("[mtrace]: read at 0x%08x, data 0x%08x\n", addr, res);
    //printf("[Trace]: MemRead at 0x%08x, res: 0x%08x\n", addr, res);
    return res;
}

uint32_t pmem_write(paddr_t addr, uint32_t data, uint32_t mask){
    uint32_t host_index = addr - MEMBASE;
    npc_alert(host_index + 3 < MEMSIZE);
    uint32_t res = host_write(guest2host(addr), data, mask);
    printf("[mtrace]: write at 0x%08x, data 0x%08x, mask 0x%x, real_write: 0x%08x\n", addr, data, mask, res);
    return res;
}
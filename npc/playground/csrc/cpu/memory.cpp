#include "common.h"
#include "cpu.h"
#include "device.h"
#include "memory.h"
#include "dpi.h"
#include "setting.h"
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
    uint32_t real_data = *(uint32_t *)addr & (~real_mask) | data & real_mask;
    *(uint32_t *)addr = real_data;
    return real_data;
}

void mtrace_read(uint32_t addr, uint32_t res){
    if (MTRACE){
        printf("[mtrace]: read at 0x%08x, data 0x%08x\n", addr, res);
    }
}

void mtrace_write(uint32_t addr, uint32_t data, uint32_t mask, uint32_t res){
    if (MTRACE)
        printf("[mtrace]: write at 0x%08x, data 0x%08x, mask 0x%x, real_write: 0x%08x\n", addr, data, mask, res);
}

void dtrace_read(uint32_t addr, uint32_t res){
    if (DTRACE){
        printf("[dtrace]: read at 0x%08x, data 0x%08x\n", addr, res);
    }
}

void dtrace_write(uint32_t addr, uint32_t data, uint32_t mask, uint32_t res){
    if (DTRACE)
        printf("[dtrace]: write at 0x%08x, data 0x%08x, mask 0x%x, real_write: 0x%08x\n", addr, data, mask, res);
}

uint32_t pmem_read(paddr_t addr){
    uint32_t res;
    bool suc;
    res = try_device_read(addr, &suc);
    if (suc){
        dtrace_read(addr, res);
        return res;
    }

    uint32_t host_index = addr - MEMBASE;
    npc_alert(host_index + 3 < MEMSIZE);
    if (npc_state != CPU_RUN)
        npc_val = addr;
    npc_val = addr;
    res = host_read(guest2host(addr));
    mtrace_read(addr, res);
    //printf("[Trace]: MemRead at 0x%08x, res: 0x%08x\n", addr, res);
    return res;
}

uint32_t pmem_write(paddr_t addr, uint32_t data, uint32_t mask){
    uint32_t res;
    bool suc;
    res = try_device_write(addr, data, mask, &suc);
    if (suc){
        dtrace_write(addr, data, mask, res);
        return res;
    }

    uint32_t host_index = addr - MEMBASE;
    npc_alert(host_index + 3 < MEMSIZE);
    if (npc_state != CPU_RUN)
        npc_val = addr;
    res = host_write(guest2host(addr), data, mask);
    mtrace_write(addr, data, mask, res);
    return res;
}
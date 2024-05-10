#include "common.h"
#include "cpu.h"
#include "device.h"
#include "memory.h"
#include "dpi.h"
#include "setting.h"
#include <cassert>
#include <cstdio>
#include <cstdint>

uint8_t pmem[MEMSIZE];
uint8_t mrom[MROM_SIZE];
uint8_t sram[SRAM_SIZE];
uint8_t flash[FLASH_SIZE];

void flash_init(){
    uint32_t *ptr = (uint32_t *)flash;
    ptr[0] = 0x100007b7;
    ptr[1] = 0x04100713;
    ptr[2] = 0x00e78023;
    ptr[3] = 0x0000006f;
}

void init_mem(){
#ifdef __SOC__
    flash_init();
#endif
}

uint8_t* guest2host(paddr_t paddr){
    if (in_pmem(paddr))
        return pmem + paddr - MEMBASE;
    if (in_mrom(paddr))
        return mrom + paddr - MROM_BASE;
    if (in_sram(paddr))
        return sram + paddr - SRAM_BASE;
    if (in_flash(paddr))
        return flash + paddr - FLASH_BASE;
    npc_info = paddr;
    npc_alert(0, CPU_ABORT_MEMLEAK);
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
        fprintf(flash_log, "[mtrace]: read at 0x%08x, data 0x%08x\n", addr, res);
    }
}

void mtrace_write(uint32_t addr, uint32_t data, uint32_t mask, uint32_t res){
    if (MTRACE)
        fprintf(flash_log, "[mtrace]: write at 0x%08x, data 0x%08x, mask 0x%x, real_write: 0x%08x\n", addr, data, mask, res);
}

uint32_t pmem_read(paddr_t addr){
    uint32_t res = 0;

    res = host_read(guest2host(addr));
    mtrace_read(addr, res);
    return res;
}

uint32_t pmem_write(paddr_t addr, uint32_t data, uint32_t mask){
    uint32_t res = 0;
    if (try_device_write(addr, &data, mask))
        return 1;
    res = host_write(guest2host(addr), data, mask);
    mtrace_write(addr, data, mask, res);
    return res;
}
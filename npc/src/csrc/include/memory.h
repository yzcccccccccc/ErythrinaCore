#ifndef _MEMORY_H__
#define _MEMORY_H__

#include <cstdint>
#include "common.h"
typedef uint32_t paddr_t;

// TODO: only for simulation...
extern uint8_t pmem[MEMSIZE];
extern uint8_t mrom[MROM_SIZE];
extern uint8_t sram[SRAM_SIZE];
extern uint8_t flash[FLASH_SIZE];

static inline bool in_pmem(paddr_t addr){
    return addr >= MEMBASE && addr - MEMBASE < MEMSIZE;
}

static inline bool in_mrom(paddr_t addr){
    return addr >= MROM_BASE && addr - MROM_BASE < MROM_SIZE;
}

static inline bool in_sram(paddr_t addr){
    return addr >= SRAM_BASE && addr - SRAM_BASE < SRAM_SIZE;
}

static inline bool in_flash(paddr_t addr){
    return addr >= FLASH_BASE && addr - FLASH_BASE < FLASH_SIZE;
}

extern void init_mem();

extern uint8_t *guest2host(paddr_t paddr);
extern uint32_t pmem_read(paddr_t addr);
extern uint32_t pmem_write(paddr_t addr, uint32_t data, uint32_t mask);

#endif
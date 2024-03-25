#ifndef _MEMORY_H__
#define _MEMORY_H__

#include <cstdint>
typedef uint32_t paddr_t;

extern uint8_t *guest2host(paddr_t paddr);
extern uint32_t pmem_read(paddr_t addr);
extern uint32_t pmem_write(paddr_t addr, uint32_t data, uint32_t mask);

#endif
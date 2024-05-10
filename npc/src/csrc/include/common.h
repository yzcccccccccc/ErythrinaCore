#ifndef __COMMON_H__
#define __COMMON_H__

#include <cstdint>
#include "setting.h"
#ifdef __SOC__
#define PC_RSTVEC   0x30000000
#endif

#ifdef __SIM__
#define PC_RSTVEC  0x80000000
#endif

#define MEMBASE     0x80000000               
#define MEMSIZE     0x8000000

#define FLASH_BASE  0x30000000
#define FLASH_SIZE  0x100000

#define MROM_BASE   0x20000000
#define MROM_SIZE   0x1000

#define SRAM_BASE   0x0f000000
#define SRAM_SIZE   0x2000

#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))

// Default Inst
static const uint32_t default_inst[] = {
    0x00100093,     // addi x1, x0, 1
    0x00100073      // ebreak
};

// inst size
extern long img_size;

// Font
static const char FontYellow[]  = "\033[1;33m";
static const char FontRed[]     = "\033[1;31m";
static const char FontGreen[]   = "\033[1;32m";
static const char FontBlue[]    = "\033[34m";
static const char Restore[]     = "\033[0m";

#endif
#ifndef __COMMON_H__
#define __COMMON_H__

#include <cstdint>
#define PC_RSTVEC   0x80000000
#define MEMBASE     0x80000000               
#define MEMSIZE     0x8000000     

#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))

// Default Inst
static const uint32_t default_inst[] = {
    0x00100093,     // addi x1, x0, 1
    0x00100073      // ebreak
};

// Font
static const char FontRed[]     = "\033[1;31m";
static const char FontGreen[]   = "\033[1;32m";
static const char FontBlue[]    = "\033[34m";
static const char Restore[]     = "\033[0m";

#endif
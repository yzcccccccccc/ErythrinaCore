#ifndef _UTIL_H__
#define _UTIL_H__

#include <cstdint>
extern void init_env(int argc, char **argv);

// disasm
extern "C" void init_disasm(const char *triple);
extern "C" void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
#endif
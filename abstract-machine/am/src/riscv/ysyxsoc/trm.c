#include <am.h>
#include <klib-macros.h>
#include "ysyxsoc.h"

extern char _heap_start;
int main(const char *args);

extern char _mrom_start;
// TODO Fix
#define SZ  0x100
#define END ((uintptr_t)&_mrom_start + SZ)

Area heap = RANGE(&_heap_start, END);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

void putch(char ch) {
  outb(UART_PORT, ch);
}

#define ysyxsoc_trap(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))

void halt(int code) {
  ysyxsoc_trap(code);
  while(1);
}

void _trm_init() {
  int ret = main(mainargs);
  halt(ret);
}
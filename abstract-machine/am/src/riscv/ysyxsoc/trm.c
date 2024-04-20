#include <am.h>
#include <klib-macros.h>
#include "ysyxsoc.h"

extern char _heap_start;
int main(const char *args);

// TODO Fix
#define SZ  0x100
#define END ((uintptr_t)&_heap_start + SZ)

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

extern char _mrom_data_start, _data_start, _data_end;
extern char _bss_start, _bss_end;
void bootloader(){
  char *src = &_mrom_data_start;
  char *dst = &_data_start;
  while (dst < &_data_end)
    *dst++ = *src++;

  for (dst = &_bss_start; dst < &_bss_end; dst++){
    *dst = 0;
  }
}

void _trm_init() {
  bootloader();
  int ret = main(mainargs);
  halt(ret);
}
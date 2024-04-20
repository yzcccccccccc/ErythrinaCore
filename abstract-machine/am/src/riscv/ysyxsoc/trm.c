#include <am.h>
#include <klib-macros.h>
#include "ysyxsoc.h"

extern char _heap_start;
int main(const char *args);

// TODO Fix
#define SZ  0x2000
#define END ((uintptr_t)&_heap_start + SZ)

Area heap = RANGE(&_heap_start, END);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

void putch(char ch) {
  volatile uint8_t* uart_lsr = (volatile uint8_t *)UART_LSR;
  while (~(volatile uint8_t)(*uart_lsr) & (1 << 5));
  outb(UART_THR, ch);
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

void uart_init(){
  // Enable DLAB
  SET_BIT(UART_LCR, 7);

  // Set DLB  (Baud Rate: 600)
  outb(UART_DLB1, 0x00);
  outb(UART_DLB0, 0xc0);

  // Disable DLAB
  CLR_BIT(UART_LCR, 7);

  // Reset FIFO
  SET_BIT(UART_FCR, 1);
  SET_BIT(UART_FCR, 2);

}

void _trm_init() {
  bootloader();
  uart_init();
  int ret = main(mainargs);
  halt(ret);
}
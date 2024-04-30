#include <am.h>
#include <klib-macros.h>
#include "ysyxsoc.h"

extern char _heap_start;
int main(const char *args);

// TODO Fix
#define SZ  0x100000
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

void puts(char *str){
  while (*str){
    putch(*str++);
  }
}

#define ysyxsoc_trap(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))

void halt(int code) {
  ysyxsoc_trap(code);
  while(1);
}


void put_num(int x){
  if (x == 0){
    putch('0');
    return;
  }
  if (x < 0){
    putch('-');
    x = -x;
  }
  char buf[20];
  int i = 0;
  while (x){
    buf[i++] = x % 10 + '0';
    x /= 10;
  }
  while (i--){
    putch(buf[i]);
  }

}

void hello_info(){
  puts("Hello, welcome to ");
  // Read CSR (mvendorid & marchid) value
  uint32_t mvendorid = 0, marchid = 0;
  asm volatile("csrr %0, 0xF11" : "=r"(mvendorid));
  asm volatile("csrr %0, 0xF12" : "=r"(marchid));
  for (int i = 0; i < 4; i++){
    putch((mvendorid >> (8 * (3 - i))) & 0xff);
  }
  putch('_');
  put_num(marchid);
  puts(" (ErythrinaCore) :D\n\n");
}

void _trm_init() {
  hello_info();
  int ret = main(mainargs);
  halt(ret);
}
#include <common.h>
#include "syscall.h"

int SYS_yield(){
  // TODO: add process switch?
  Log("Sys Yielding...");
  return 0;
}

void SYS_exit(){
  halt(0);
}

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;

  switch (a[0]) {
    case 0:
      SYS_exit();
      break;
    case 1:
      c->GPR2 = SYS_yield();      // write return val to a0
      break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}

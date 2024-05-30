#include <common.h>
#include "syscall.h"

int sys_yield(){
  // TODO: add process switch?
  Log("Sys Yielding...");
  return 0;
}

void sys_exit(){
  halt(0);
}

int sys_write(int fd, void *buf, int count){
  if (fd == 1 || fd == 2){
    char *ptr = buf;
    for (int i = 0; i < count ; i++)
      putch(ptr[i]);
    return count;
  }
  else{
    Log("Unknown fd = %d", fd);
    return -1;
  }
}

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;   // a7
  a[1] = c->GPR2;   // a0
  a[2] = c->GPR3;   // a1
  a[3] = c->GPR4;   // a2 

  switch (a[0]) {
    case SYS_exit :
      sys_exit();
      break;
    case SYS_yield :
      c->GPR2 = sys_yield();      // write return val to a0
      break;
    case SYS_write:
      c->GPR2 = sys_write(a[1], (void *)a[2], a[3]);
      break;
    default: panic("Unhandled syscall ID = %d", (int)a[0]);
  }
}

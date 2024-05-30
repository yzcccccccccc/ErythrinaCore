#include <stdint.h>
#include <unistd.h>

#ifdef __ISA_NATIVE__
#error can not support ISA=native
#endif

#define SYS_yield 1
extern int _syscall_(int, uintptr_t, uintptr_t, uintptr_t);

int main() {
  char test[2];
  test[0] = _syscall_(SYS_yield, 0, 0, 0) + '0';
  test[1] = '\0';
  return write(1, test, 2);
}

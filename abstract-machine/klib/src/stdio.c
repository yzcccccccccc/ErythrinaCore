#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  char vsbuf[100];
  int cnt = 0;
  char *s;
  int d;

  while (*fmt){
    if (*fmt != '%'){
      *out = *fmt;
      out++;
    }
    else{
      fmt++;
      switch (*fmt){
        case 's':
          cnt++;
          s = va_arg(ap, char*);
          while (*s != '\0'){
            *out = *s;
            out++;
            s++;
          }
          break;
        case 'd':
          cnt++;
          d = va_arg(ap, int);
          int len = 0;
          if (d < 0){
            d = -d;
            *out = '-';
            out++;
          }
          if (d == 0){
            *out = '0';
            out++;
          }
          else{
            for (;d > 0; d /= 10, len++){
              vsbuf[len] = d % 10 + '0';
            }
            for (int i = 0; i < len; i++){
              *out = vsbuf[len - i - 1];
              out++;
            }
          }
          break;
      }
    }
    fmt++;
  }
  *out = '\0';
  return cnt;
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  va_start(ap, fmt);
  int cnt = vsprintf(out, fmt, ap);
  va_end(ap);
  return cnt;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif

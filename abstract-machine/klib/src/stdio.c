#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

#define BUFLEN 400

void my_print_num(int num){
  if (num < 0){
    putch('-');
    num = -num; 
  }
  if (num < 10){
    putch('0' + num);
    return;
  }
  my_print_num(num / 10);
  putch('0' + num % 10);
  return;
}

int printf(const char *fmt, ...) {
  char pbuf[BUFLEN];

  // write to pbuf
  va_list ap;
  va_start(ap, fmt);
  int len = vsprintf(pbuf, fmt, ap);
  va_end(ap);
  assert(len < BUFLEN);

  // write to port
  for (int i = 0; pbuf[i] != '\0'; i++)
    putch(pbuf[i]);
  return len;
}

char *int2str(long val, char *out, int pad_len, char pad_ch){
  if (val < 0){
    *out = '-';
    val = -val;
    out++;
  }
  char vsbuf[BUFLEN];
  int len = 0;
  for (;val > 0; val /= 10, len++){
    vsbuf[len] = val % 10 + '0';
  }
  for (int i = len; i < pad_len; i++){
    *out = pad_ch;
    out++;
  }
  for (int i = 0; i < len; i++){
    *out = vsbuf[len - i - 1];
    out++;
  }
  return out;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  char *s;
  int pad_len = 0, pad_ch = 0;
  char *out_in = out;
  long d;

  while (*fmt){
    if (*fmt != '%'){
      *out = *fmt;
      out++;
    }
    else{
      fmt++;
      switch (*fmt){
        case 's':
          s = va_arg(ap, char*);
          while (*s != '\0'){
            *out = *s;
            out++;
            s++;
          }
          break;
        case 'd': case 'x':   // TODO: Add Hex!
          d = va_arg(ap, int);
          out = int2str(d, out, pad_len, pad_ch);
          pad_len = 0;
          break;
        case 'l':
          fmt++;
          assert(*fmt == 'd' || *fmt == 'x');
          d = va_arg(ap, long);
          break;
        case 'c':
          d = va_arg(ap, int);
          *out = d;
          out++;
          break;
        case '0': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
        // format 0...
          if (*fmt == '0')
            pad_ch = '0';
          else
            pad_ch = ' ';
          pad_len = *fmt - '0';
          fmt++;
          while (*fmt != 'd' && *fmt != 'x' && *fmt != '\0'){
            pad_len = pad_len * 10 + (*fmt - '0');
            fmt++;
          }
          assert(*fmt != '\0');
          assert(pad_len != 0);
          fmt--;
          break;
        default:
          printf("%s", fmt);
          assert(0);
      }
    }
    fmt++;
  }
  *out = '\0';
  return (out - out_in);
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  va_start(ap, fmt);
  int len = vsprintf(out, fmt, ap);
  va_end(ap);
  assert(len < BUFLEN);
  return len;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif

#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

#define BUFLEN 10000

void my_print_num(long num){
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
char pbuf[BUFLEN];
int printf(const char *fmt, ...) {

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

char *int2str(long val, char *out, int pad_len, char pad_ch, int base){
  assert(base == 10 || base == 16);
  if (val < 0){
    *out = '-';
    val = -val;
    out++;
  }
  char vsbuf[BUFLEN];
  int len = 0;
  if (val == 0)
    vsbuf[len++] = '0';
  else
    for (;val > 0; val /= base, len++){
      if (base == 10)
        vsbuf[len] = val % 10 + '0';
      if (base == 16)
        vsbuf[len] = val % 16 < 10 ? val % 16 + '0' : val % 16 + 'a' - 10;
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

#define sIDLE     0
#define sPREP     1
#define sSTRING   2
#define sLONG     3
#define sINT      4
#define sCHAR     5
  int state = sIDLE;

  while (*fmt){
    switch (state){
      case sIDLE:{
        if (*fmt == '%'){
          state = sPREP;
          fmt++;
        }
        else{
          *out = *fmt;
          out++;
          fmt++;
        }
        break;
      }
      case sPREP:{
        if ('0' <= *fmt && *fmt <= '9'){
          if (*fmt == '0'){
            pad_ch = '0';
          }
          else{
            pad_ch = ' ';
          }
          pad_len = *fmt - '0';
          fmt++;
          while (*fmt != 'd' && *fmt != 'x' && *fmt != 'l' && *fmt != '\0'){
            pad_len = pad_len * 10 + (*fmt - '0');
            fmt++;
          }
        }
        switch (*fmt){
          case 's':
            state = sSTRING;
            break;
          case 'd': case 'x':
            state = sINT;
            break;
          case 'p':
            state = sLONG;
            fmt--;
            break;
          case 'l':
            state = sLONG;
            break;
          case 'c':
            state = sCHAR;
            break;
          default:
            printf("%s", fmt);
            assert(0);
        }
        break;
      }
      case sSTRING:{
        s = va_arg(ap, char*);
        while (*s != '\0'){
          *out = *s;
          out++;
          s++;
        }
        state = sIDLE;
        fmt++;
        break;
      }
      case sINT:{
        d = va_arg(ap, int);
        out = int2str(d, out, pad_len, pad_ch, *fmt == 'd' ? 10 : 16);
        state = sIDLE;
        pad_len = 0;
        fmt++;
        break;
      }
      case sLONG:{
        fmt++;
        assert(*fmt == 'd' || *fmt == 'x' || *fmt == 'p');
        if (*fmt == 'p'){
          *out = '0';
          out++;
          *out = 'x';
          out++;
        }
        d = va_arg(ap, long);
        out = int2str(d, out, pad_len, pad_ch, *fmt == 'd' ? 10 : 16);
        state = sIDLE;
        pad_len = 0;
        fmt++;
        break;
      }
      case sCHAR:{
        d = va_arg(ap, int);
        *out = d;
        out++;
        state = sIDLE;
        fmt++;
        break;
      }
      default:
        assert(0);
    }
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

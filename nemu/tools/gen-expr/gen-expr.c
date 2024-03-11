/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

clock_t start, end;

// this should be enough
int buf_cur, buf_remain;
#define BUFLEN 100
#define GENTIME 4000
static char buf[BUFLEN];
static char code_buf[BUFLEN + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n"
"int main() { "
"  unsigned result = %s; "
"  printf(\"%%u\", result); "
"  return 0; "
"}";

long choose(long n){
  return rand() % n;
}

int getlen(long n){
  if (n == 0)
    return 1;
  int len = 0;
  while (n){
    n /= 10;
    len++;
  }
  return len;
}

long gen_num(){
  long num, bound = 1000;
  int len;
  for (;;){
    //printf("%d ", bound);
    num = choose(bound);
    len = getlen(num);
    if (len <= buf_remain){
      sprintf((buf + buf_cur), "%ld", num);
      buf_cur += len;
      buf_remain -= len;
      return num;
    }
    else{
      bound = 1;
      for (int i = 0; i < buf_remain; i++)  bound *= 10;
    }
  }
}

enum{
  OP_ADD,
  OP_SUB,
  OP_MUL,
  OP_DIV
};

int gen_rand_op(){
  int rt = choose(4);
  switch (rt) {
    case OP_ADD:
      buf[buf_cur] = '+';
      break;
    case OP_SUB:
      buf[buf_cur] = '-';
      break;
    case OP_DIV:
      buf[buf_cur] = '/';
      break;
    case OP_MUL:
      buf[buf_cur] = '*';
      break;
  }
  buf_cur++;
  buf_remain--;
  return rt;
}

static long gen_rand_expr() {
  long res = 0;
  int opt = choose(3);
  if ((end = clock()) - start > GENTIME || buf_remain <= 2){
    opt = 0;
  }
  switch (opt) {
    case 0:{
      res = gen_num();
      break;
    }
    case 1:{
      if (buf_remain >= 3){
        buf_remain -= 2;    // '('')'
        buf[buf_cur++] = '(';
        res = gen_rand_expr();
        buf[buf_cur++] = ')';
      }
      break;
    }
    case 2:{
      if (buf_remain >= 3){
        int cur = buf_cur, remain = buf_remain;
        long val1 = gen_rand_expr(), val2;
        while (buf_remain < 2 || val1 < 0){
          buf_cur = cur;
          buf_remain = remain;
          val1 = gen_rand_expr();
        }
        int op = gen_rand_op();
        if (op == OP_DIV){
          cur = buf_cur;
          remain = buf_remain;
          while ((val2 = gen_rand_expr()) <= 0){
            buf_cur = cur;
            buf_remain = remain;
          }
        }
        else{
          cur = buf_cur;
          remain = buf_remain;
          while ((val2 = gen_rand_expr()) < 0){
            buf_cur = cur;
            buf_remain = remain;
          }
        }
        switch (op) {
          case OP_ADD:
            res = val1 + val2;
            break;
          case OP_SUB:
            res = val1 - val2;
            break;
          case OP_MUL:
            res = val1 * val2;
            break;
          case OP_DIV:
            res = val1 / val2;
            break;
        }
      }
      break;
    }
  }
  return res;
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);
  int loop = 10;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop; i ++) {
    buf_cur = 0;
    buf_remain = BUFLEN;
    start = clock();
    gen_rand_expr();
    buf[buf_cur] = '\0';

    sprintf(code_buf, code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc /tmp/.code.c -o /tmp/.expr -Werror");
    if (ret != 0) continue;

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    ret = fscanf(fp, "%d", &result);
    pclose(fp);

    printf("%u %s\n", result, buf);
  }
  return 0;
}

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

#include "common.h"
#include "memory/paddr.h"
#include <assert.h>
#include <isa.h>

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>

enum {
  TK_NOTYPE = 256, TK_EQ,

  /* TODO: Add more token types */
  TK_NUM,
  TK_REG,
  TK_ADD,
  TK_DIV,
  TK_MUL,
  TK_SUB,
  TK_LPAR,
  TK_RPAR,
  TK_UNEQ,
  TK_AND,
  TK_DEREF,
};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+", TK_ADD},         // plus
  {"\\/", TK_DIV},         // devide
  {"-", TK_SUB},           // subtract
  {"\\*", TK_MUL},         // multiply
  {"\\(", TK_LPAR},         // left parathesis
  {"\\)", TK_RPAR},         // right parathesis
  {"==", TK_EQ},        // equal
  {"!=", TK_UNEQ},      // !=
  {"&&", TK_AND},       // &&
  {"0[xX][0-9a-fA-F]+|0[oO]?[0-7]+|0[bB][01]+|[1-9][0-9]*|0", TK_NUM},      // numbers
  {"\\$[a-zA-Z0-9]+", TK_REG},         // regs

};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[1024] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        switch (rules[i].token_type) {
          case TK_NOTYPE: break;
          case TK_NUM: case TK_REG:
            tokens[nr_token].type = rules[i].token_type;
            memcpy(tokens[nr_token].str, e + position - substr_len, substr_len);
            tokens[nr_token].str[substr_len] = '\0';
            nr_token++;
            break;
          default:
            tokens[nr_token++].type = rules[i].token_type;
        }

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}

bool check_parentheses(int p, int q){
  int dep = 0;
  for (int i = p; i <= q; i++){
    if (tokens[i].type == TK_LPAR){
      dep++;
      continue;
    }
    if (tokens[i].type == TK_RPAR){
      dep--;
      continue;
    }
    if (dep == 0 && i != q)
      return false;         // like () <ope> ()
  }
  assert(dep == 0);
  return true;
}

int get_priority(int type){
  if (type == TK_MUL || type == TK_DIV)
    return 2;
  else
    if (type == TK_ADD || type == TK_SUB)
      return 1;
    else
      return 0;
}

int is_operator(int type){
  return type == TK_MUL || type == TK_DIV || type == TK_ADD || type == TK_SUB || type == TK_EQ || type == TK_UNEQ;
}

int get_op_position(int p, int q, int *type){
  int dep = 0;
  int cur = p, cur_priority = 3, cur_type = -1;
  for (int i = p, priority; i <= q; i++){
    if (tokens[i].type== TK_NOTYPE) continue;
    if (tokens[i].type == TK_LPAR){
      dep++;
      continue;
    }
    if (tokens[i].type == TK_RPAR){
      dep--;
      continue;
    }
    if (dep != 0) continue;
    if (tokens[i].type == TK_NUM || tokens[i].type == TK_REG || tokens[i].type == TK_DEREF) continue;     // numbers or deref
    if (!is_operator(tokens[i].type)){
      *type = -1;
      assert(0);
      return -1;
    }
    if ((priority = get_priority(tokens[i].type)) <= cur_priority){
      cur_priority = priority;
      cur = i;
      cur_type = tokens[i].type;
    }
  }
  *type = cur_type;
  return cur;
}

uint32_t eval(int p, int q, bool *success){
  if (*success == 0)
    return 0;
  if (p > q){
    *success = 0;
    return 0;
  }
  else{
    if (p == q){
      assert(tokens[p].type == TK_NUM || tokens[p].type == TK_REG);
      uint32_t res = 0;
      if (tokens[p].type == TK_NUM){
        res = (uint32_t)(strtol(tokens[p].str, NULL, 0));
        assert(errno == 0);
      }
      if (tokens[p].type == TK_REG){
        bool suc = 0;
        res = (uint32_t)(isa_reg_str2val((tokens[p].str + 1), &suc));
        //assert(suc == 1);
        *success = suc;
      }
      return res;
    }
    else 
      if (check_parentheses(p, q)){
        return eval(p + 1, q - 1, success);
      }
      else{
        if (tokens[p].type == TK_DEREF){
          uint32_t addr = eval(p + 1, q, success);
          if (in_pmem(addr)){
            return paddr_read(addr, 4);
          }
          else{
            printf("Invlaid address: 0x%x\n", addr);
            *success = 0;
            return 0;
          }
        }
        else{
          int optype;
          int op = get_op_position(p, q, &optype);
          assert(optype != -1);
          uint32_t val1 = eval(p, op - 1, success);
          uint32_t val2 = eval(op + 1, q, success);
          switch(optype){
            case TK_ADD:
              return val1 + val2;
              break;
            case TK_SUB:
              return val1 - val2;
              break;
            case TK_MUL:
              return val1 * val2;
              break;
            case TK_DIV:
              if (val2 == 0){
                printf("Div0 error.\n");
                success = 0;
                return 0;
              }
              else {
                return val1 / val2;
              }
              break;
            default: assert(0);
          }
        }
      }
  }
}


word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
  for (int i = 0; i < nr_token; i++){
    if (tokens[i].type == TK_MUL && (i == 0 || is_operator(tokens[i - 1].type)))
      tokens[i].type = TK_DEREF;
  }

  *success = 1;
  word_t res = eval(0, nr_token - 1, success);

  return res;
}

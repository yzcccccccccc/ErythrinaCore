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

#include <stdio.h>
#include <isa.h>
#include <cpu/cpu.h>
#include <readline/readline.h>
#include <readline/history.h>
#include "sdb.h"
#include <memory/paddr.h>
#include <string.h>
#include <utils.h>

static int is_batch_mode = false;

void init_regex();
void init_wp_pool();

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nemu) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}


static int cmd_q(char *args) {
  nemu_state.state = NEMU_QUIT;
  printf("Goodbye! iwi\n");
  return -1;
}

static int cmd_help(char *args);\

static int cmd_si(char *args);\

static int cmd_info(char *args);\

static int cmd_x(char *args);\

static int cmd_p(char *args);\

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },
  { "si", "si [N]. Continue N steps of the program, default is 1", cmd_si },
  { "info", "info r/w. show reg/watch point info", cmd_info},
  { "x", "x N EXPR. show memory datas of 4B * N beginning with EXPR", cmd_x},
  { "p", "p EXPR. calculate the result of EXPR", cmd_p},
  /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

static int cmd_si(char *args){
  int n = 0;
  char *arg = strtok(NULL, " ");
  if (arg == NULL){
    // no args: step 1!
    n = 1;
  }
  else{
    int ite = 0;
    while (*arg != '\0'){
      n = n * 10 + (*arg - '0');
      ite ++;
      if (ite > 10)
        return 1;
    }
  }
  cpu_exec(n);
  return 0;
}

static int cmd_info(char *args){
  char *arg = strtok(NULL, " ");
  if (arg == NULL){
    printf("Insufficient args!\n");
    return 1;
  }
  if (*arg == 'r'){
    isa_reg_display();
    return 0;
  }
  if (*arg == 'w'){
    watchpoint_display();
    return 0;
  }
  printf("Unknonw parameters (r or w!)\n");
  return 1;
}

static int cmd_x(char *args){
  char *arg1 = strtok(NULL, " ");
  char *arg2 = strtok(NULL, " ");
  int N, EXPR;          // row version =v=
  if (arg1 == NULL || arg2 == NULL){
    printf("Insufficient args.\n");
  }
  else{
    sscanf(arg1, "%d", &N);
    sscanf(arg2, "%x", &EXPR);
    for (int i = 0, addr = EXPR; i < N; i++, addr += 4){
      if (in_pmem(addr))
        printf("0x%08x: 0x%08x\n", addr, paddr_read(addr, 4));
      else{
        printf("Exit because of out of boundary [0x%x,0x%x].\n", PMEM_LEFT, PMEM_RIGHT);
        break;
      }
    }
  }
  return 0;
}

int cmd_p(char *args){
  bool success = 0;
  char *e = strtok(NULL, "\n");
  uint32_t res = expr(e, &success);
  if (success){
    printf("Res:0x%x (or %lu)\n", res, (long)res);
    return 1;
  }
  else{
    printf("Invalid expression.\n");
    return 0;
  }
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}

#include "sdb.h"
#include "common.h"
#include "cpu.h"
#include <cstdlib>
#include <readline/readline.h>
#include <readline/history.h>

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(npc) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

int is_batch_mode = 0;
void set_batch_mode(){
    is_batch_mode = 1;
}

void init_sdb(){
    printf("\n%sWelcome to NPC Simulator! :)%s\n", FontBlue, Restore);
}

static int cmd_q(char *args) {
  printf("%sGoodbye! iwi%s\n", FontGreen, Restore);
  return -1;
}

static int cmd_c(char *args);
static int cmd_help(char *args);
static int cmd_si(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
    { "help", "Display information about all supported commands", cmd_help },
    { "c", "Continue the execution of the program", cmd_c },
    { "si", "si [N]. Continue N steps of the program, default is 1", cmd_si },
    { "q", "Exit NPC Simulator", cmd_q },
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

static int cmd_c(char *args){
    execute(-1);
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
  execute(n);
  return 0;
}

void sdb_main(){
    init_cpu();
    init_sdb();

    if (is_batch_mode){
      execute(-1);
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

        int i;
        for (i = 0; i < NR_CMD; i ++) {
        if (strcmp(cmd, cmd_table[i].name) == 0) {
            if (cmd_table[i].handler(args) < 0) { return; }
            break;
        }
        }

        if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
  return;
}
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

#include <common.h>
#include "monitor/sdb/sdb.h"

void init_monitor(int, char *[]);
void am_init_monitor();
void engine_start();
int is_exit_status_bad();

char buf[65536];

int main(int argc, char *argv[]) {
  /* Initialize the monitor. */
#ifdef CONFIG_TARGET_AM
  am_init_monitor();
#else
  init_monitor(argc, argv);
#endif

/* for debugging expr()
  Log("Testing expr()...\n");
  FILE *input = fopen("/home/yzcc/ysyx-workbench/nemu/tools/gen-expr/input", "r");
  uint32_t res;
  bool suc;
  while (fscanf(input, "%u %s", &res, buf) != EOF){
    uint32_t expr_res = expr(buf, &suc);
    //printf("expr:%u, std:%u\n\n", expr(buf, &suc), res);
    Log("Buf:%s, %s (%u, std:%u), %s\n", buf, (expr_res == res)?"pass":"fail", expr_res, res, suc?"success":"fail");
    assert(expr_res == res);
  }
*/

  /* Start engine. */
  engine_start();

  return is_exit_status_bad();
}

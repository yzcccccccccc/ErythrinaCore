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

#include "sdb.h"

#define NR_WP 32
#define BUFLEN 32

typedef struct watchpoint {
  int NO;
  struct watchpoint *next;

  /* TODO: Add more members if necessary */
  char buf[BUFLEN];
  uint32_t hisval;

} WP;

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

WP *new_wp(){
  if (free_ == NULL)
    return NULL;
  else{
    WP *ptr = free_;
    free_ = free_->next;
    if (head == NULL){
      head = ptr;
      ptr->next = NULL;
    }
    else{
      ptr->next = head->next;
      head->next = ptr;
    }
    return ptr;
  }
}

void free_wp(WP *wp){
  WP *pre_ptr = head;
  assert(pre_ptr != NULL);
  while (pre_ptr->next != wp){
    pre_ptr = pre_ptr->next;
    assert(pre_ptr != NULL);
  }
  pre_ptr->next = wp->next;
  if (free_ == NULL){
    free_ = wp;
    wp->next = NULL;
  }
  else{
    wp->next = free_->next;
    free_ = wp;
  }
}

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */

// return 0 for unchanged
int watchpoint_scan(){
  int change = 0;
  for (WP *ptr = head;ptr != NULL; ptr = ptr->next){
    bool suc = 1;
    uint32_t val = expr(ptr->buf, &suc);
    if (val != ptr->hisval){
      printf("Watchpoint %d (%s) changed.\n", ptr->NO, ptr->buf);
      change = 1;
    }
    ptr->hisval = val;
  }
  return change;
}

void watchpoint_display(){
  WP *ptr = head; 
  printf("[watch points]\n");
  int cnt = 0;
  while (ptr != NULL){
    printf("%03d %s (val:%u)\n", ptr->NO, ptr->buf, ptr->hisval);
    ptr = ptr->next;
    cnt ++;
  }
  printf("(%d watchpoints)\n", cnt);
}

// return number of watchppoint
int add_watchpoint(char *buf){
  WP *ptr = new_wp();
  assert(ptr != NULL);
  bool suc = 1;
  ptr->hisval = expr(buf, &suc);
  if (!suc){
    printf("Invalid expression.\n");
    return 0;
  }
  strcpy(ptr->buf, buf);
  return ptr->NO;
}

void del_watchpoint(int NO){
  for (WP *ptr = head; ptr != NULL; ptr = ptr->next){
    if (ptr->NO == NO){
      printf("Delete watchpoint %d (%s).\n", NO, ptr->buf);
      free_wp(ptr);
      return;
    }
  }
  printf("Unknown watchpoint.\n");
  return;
}
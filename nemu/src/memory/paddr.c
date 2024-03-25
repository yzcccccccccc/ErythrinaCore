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
#include "macro.h"
#include "utils.h"
#include <memory/host.h>
#include <memory/paddr.h>
#include <device/mmio.h>
#include <isa.h>

#if   defined(CONFIG_PMEM_MALLOC)
static uint8_t *pmem = NULL;
#else // CONFIG_PMEM_GARRAY
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
#endif

uint8_t* guest_to_host(paddr_t paddr) { return pmem + paddr - CONFIG_MBASE; }
paddr_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }

static word_t pmem_read(paddr_t addr, int len) {
  word_t ret = host_read(guest_to_host(addr), len);
  return ret;
}

static void pmem_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_host(addr), len, data);
}

static void out_of_bound(paddr_t addr) {
  panic("address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, cpu.pc);
}

void init_mem() {
#if   defined(CONFIG_PMEM_MALLOC)
  pmem = malloc(CONFIG_MSIZE);
  assert(pmem);
#endif
  IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
  Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
}

void mtrace(paddr_t addr, word_t res, char *info, int len){
  if (addr >= CONFIG_MTRACE_LBOUND && addr <= CONFIG_MTRACE_RBOUND)
    log_write("[mtrace] %s at 0x%x, data 0x%x, len %d\n", info, addr, res, len);
}

word_t paddr_read(paddr_t addr, int len) {
  if (likely(in_pmem(addr))){
    word_t res = pmem_read(addr, len);
    IFDEF(CONFIG_MTRACE, mtrace(addr, res, "memory read", len));
    return res;
  }
#ifdef CONFIG_DEVICE
  word_t res = mmio_read(addr, len);
  IFDEF(CONFIG_MTRACE, mtrace(addr, res, "mmio read", len));
  return res;
#endif
  out_of_bound(addr);
  return 0;
}

void paddr_write(paddr_t addr, int len, word_t data) {
  if (likely(in_pmem(addr))){
    IFDEF(CONFIG_MTRACE, mtrace(addr, data, "memory write", len));
    pmem_write(addr, len, data);
    return;
  }
#ifdef CONFIG_DEVICE
  IFDEF(CONFIG_MTRACE, mtrace(addr, data, "mmio write", len));
#endif
  out_of_bound(addr);
}

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

#if defined (CONFIG_YSYXSOC)
static uint8_t mrom[CONFIG_MROM_SIZE] PG_ALIGN = {};
static uint8_t sram[CONFIG_SRAM_SIZE] PG_ALIGN = {};
static uint8_t flash[CONFIG_FLASH_SIZE] PG_ALIGN = {};

uint8_t* guest_to_mrom(paddr_t paddr) { return mrom + paddr - CONFIG_MROM_BASE; }
paddr_t mrom_to_guest(uint8_t *haddr) { return haddr - mrom + CONFIG_MROM_BASE; }

uint8_t* guest_to_sram(paddr_t paddr) { return sram + paddr - CONFIG_SRAM_BASE; }
paddr_t sram_to_guest(uint8_t *haddr) { return haddr - sram + CONFIG_SRAM_BASE; }

uint8_t* guest_to_flash(paddr_t paddr) {return flash + paddr - CONFIG_FLASH_BASE;}
paddr_t flash_to_guest(uint8_t *haddr) {return haddr - flash + CONFIG_FLASH_BASE;}

static word_t mrom_read(paddr_t addr, int len){
  return host_read(guest_to_mrom(addr), len);
}

// only for init
static void mrom_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_mrom(addr), len, data);
}

static word_t sram_read(paddr_t addr, int len){
  return host_read(guest_to_sram(addr), len);
}

static void sram_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_sram(addr), len, data);
}

static word_t flash_read(paddr_t addr, int len){
  return host_read(guest_to_flash(addr), len);
}

// only for init
static void flash_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_flash(addr), len, data);
}

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
#ifdef CONFIG_MTRACE
  if (addr >= CONFIG_MTRACE_LBOUND && addr <= CONFIG_MTRACE_RBOUND)
    log_write("[mtrace] %s at 0x%x, data 0x%x, len %d\n", info, addr, res, len);
#endif
}

word_t paddr_read(paddr_t addr, int len) {
  if (likely(in_pmem(addr))){
    word_t res = pmem_read(addr, len);
    IFDEF(CONFIG_MTRACE, mtrace(addr, res, "memory read", len));
    return res;
  }

#if defined (CONFIG_YSYXSOC)
  if (in_mrom(addr)){
    word_t res = mrom_read(addr, len);
    IFDEF(CONFIG_MTRACE, mtrace(addr, res, "mrom read", len));
    return res;
  }

  if (in_sram(addr)){
    word_t res = sram_read(addr, len);
    IFDEF(CONFIG_MTRACE, mtrace(addr, res, "sram read", len));
    return res;
  }

  if (in_flash(addr)){
    word_t res = flash_read(addr, len);
    IFDEF(CONFIG_MTRACE, mtrace(addr, res, "sram read", len));
    return res;
  }
#endif

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

#if defined (CONFIG_YSYXSOC)
  if (in_mrom(addr)){
    IFDEF(CONFIG_MTRACE, mtrace(addr, data, "mrom write", len));
    mrom_write(addr, len, data);
    return;
  }

  if (in_sram(addr)){
    IFDEF(CONFIG_MTRACE, mtrace(addr, data, "sram write", len));
    sram_write(addr, len, data);
    return;
  }

  if (in_flash(addr)){
    IFDEF(CONFIG_MTRACE, mtrace(addr, data, "sram write", len));
    flash_write(addr, len, data);
    return;
  }
#endif

#ifdef CONFIG_DEVICE
  IFDEF(CONFIG_MTRACE, mtrace(addr, data, "mmio write", len));
  mmio_write(addr, len, data); 
  return;
#endif
  out_of_bound(addr);
}

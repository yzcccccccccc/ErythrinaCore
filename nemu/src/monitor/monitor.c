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
#include <isa.h>
#include <memory/paddr.h>
#include <stdio.h>

void init_rand();
void init_log(const char *log_file);
void init_mem();
void init_difftest(char *ref_so_file, long img_size, int port);
void init_device();
void init_sdb();
void init_disasm(const char *triple);

static void welcome() {
  Log("Trace: %s", MUXDEF(CONFIG_TRACE, ANSI_FMT("ON", ANSI_FG_GREEN), ANSI_FMT("OFF", ANSI_FG_RED)));
  IFDEF(CONFIG_TRACE, Log("If trace is enabled, a log file will be generated "
        "to record the trace. This may lead to a large log file. "
        "If it is not necessary, you can disable it in menuconfig"));
  Log("Build time: %s, %s", __TIME__, __DATE__);
  printf("Welcome to %s-NEMU!\n", ANSI_FMT(str(__GUEST_ISA__), ANSI_FG_YELLOW ANSI_BG_RED));
  printf("For help, type \"help\"\n");
  //Log("Exercise: Please remove me in the source code and compile NEMU again.");
  //assert(0);
}

#ifndef CONFIG_TARGET_AM
#include <getopt.h>

void sdb_set_batch_mode();

static char *log_file = NULL;
static char *diff_so_file = NULL;
static char *img_file = NULL;
static char *elf_file = NULL;
static int difftest_port = 1234;

#include <elf.h>
typedef MUXDEF(CONFIG_ISA64, ELF64_Ehdr, Elf32_Ehdr) Ehdr;
typedef MUXDEF(CONFIG_ISA64, ELF64_Shder, Elf32_Shdr) Shdr;
typedef MUXDEF(CONFIG_ISA64, ELF64_Sym, Elf32_Sym) Sym;
#define STRBUFLEN 10000000
char strbuf[STRBUFLEN];
int funcs_cnt;
struct funcarray funcs[FUNC_NUM];
void ftrace_init(){
  if (elf_file == NULL){
    Log("No Elf File, exiting ftrace_init...");
    return;
  }
  FILE *elf = fopen(elf_file, "rb");
  Assert(elf, "Can not open '%s'", elf_file);

  fseek(elf, 0, SEEK_SET);
  int ret;

  // Get the ELF header
  Ehdr elf_header;
  ret = fread(&elf_header, sizeof(elf_header), 1, elf);
  assert(ret == 1);
  word_t sh_offset = elf_header.e_shoff;

  // Get the .strtab offset
  Shdr sec_header;
  fseek(elf, sh_offset, SEEK_SET);
retry_str:
  ret = fread(&sec_header, sizeof(sec_header), 1, elf);
  assert(ret == 1);
  if (sec_header.sh_type != SHT_STRTAB)
    goto retry_str;
  assert(sec_header.sh_size < STRBUFLEN);
  //word_t tmp_offset = ftell(elf);
  fseek(elf, sec_header.sh_offset, SEEK_SET);
  ret = fread(strbuf, sec_header.sh_size, 1, elf);
  assert(ret == 1);
  //if (strcmp(strbuf, ".strtab")){
  //  fseek(elf, tmp_offset, SEEK_SET);
  //  goto retry_str;
  //}

  // Get the .symtab offset
  fseek(elf, sh_offset, SEEK_SET);
retry_sym:
  ret = fread(&sec_header, sizeof(sec_header), 1, elf);
  assert(ret == 1);
  if (sec_header.sh_type != SHT_SYMTAB)
    goto retry_sym;
  word_t sym_offset = sec_header.sh_offset;
  word_t sym_size = sec_header.sh_size;

  // initialize function names
  Sym sym_entry;
  fseek(elf, sym_offset, SEEK_SET);
  for (word_t rd_sz = 0; rd_sz < sym_size; rd_sz += sizeof(sym_entry)){
    ret = fread(&sym_entry, sizeof(sym_entry), 1, elf);
    assert(ret == 1);
    if ((sym_entry.st_info & 0xf) == STT_FUNC){
      assert(funcs_cnt < FUNC_NUM);
      funcs[funcs_cnt].entry_point = sym_entry.st_value;
      strcpy(funcs[funcs_cnt].name, (strbuf + sym_entry.st_name));
      funcs[funcs_cnt].size = sym_entry.st_size;
      Log("[ftrace init] Find func %s at 0x%x, size 0x%x", 
        funcs[funcs_cnt].name,
        funcs[funcs_cnt].entry_point,
        funcs[funcs_cnt].size
      );
      funcs_cnt++;
    }
  }
}

static long load_img() {
  if (img_file == NULL) {
    Log("No image is given. Use the default build-in image.");
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  Assert(fp, "Can not open '%s'", img_file);

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  Log("The image is %s, size = %ld", img_file, size);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);

#ifdef CONFIG_FTRACE
  Log("Parsing ELF File, ftrace initializing...");
  ftrace_init();
#endif

  fclose(fp);
  return size;
}

static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"diff"     , required_argument, NULL, 'd'},
    {"port"     , required_argument, NULL, 'p'},
    {"help"     , no_argument      , NULL, 'h'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhl:d:p:e:", table, NULL)) != -1) {
    switch (o) {
      case 'b': sdb_set_batch_mode(); break;
      case 'p': sscanf(optarg, "%d", &difftest_port); break;
      case 'l': log_file = optarg; break;
      case 'd': diff_so_file = optarg; break;
      case 'e': elf_file = optarg; break;
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\t-l,--log=FILE           output log to FILE\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

void init_monitor(int argc, char *argv[]) {
  /* Perform some global initialization. */

  /* Parse arguments. */
  parse_args(argc, argv);

  /* Set random seed. */
  init_rand();

  /* Open the log file. */
  init_log(log_file);

  /* Initialize memory. */
  init_mem();

  /* Initialize devices. */
  IFDEF(CONFIG_DEVICE, init_device());

  #ifndef CONFIG_ISA_loongarch32r
    IFDEF(CONFIG_ITRACE, init_disasm(
      MUXDEF(CONFIG_ISA_x86,     "i686",
      MUXDEF(CONFIG_ISA_mips32,  "mipsel",
      MUXDEF(CONFIG_ISA_riscv,
        MUXDEF(CONFIG_RV64,      "riscv64",
                                "riscv32"),
                                "bad"))) "-pc-linux-gnu"
    ));
  #endif

  /* Perform ISA dependent initialization. */
  init_isa();

  /* Load the image to memory. This will overwrite the built-in image. */
  long img_size = load_img();

  /* Initialize differential testing. */
  init_difftest(diff_so_file, img_size, difftest_port);

  /* Initialize the simple debugger. */
  init_sdb();

  /* Display welcome message. */
  welcome();
}
#else // CONFIG_TARGET_AM
static long load_img() {
  extern char bin_start, bin_end;
  size_t size = &bin_end - &bin_start;
  Log("img size = %ld", size);
  memcpy(guest_to_host(RESET_VECTOR), &bin_start, size);
  return size;
}

void am_init_monitor() {
  init_rand();
  init_mem();
  init_isa();
  load_img();
  IFDEF(CONFIG_DEVICE, init_device());
  welcome();
}
#endif

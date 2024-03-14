#include "cpu/cpu.h"
#include <isa.h>
#include <memory/paddr.h>

#define INIT_TEST(name) do{memcpy(guest_to_host(RESET_VECTOR), name, sizeof(name));restart();} while(0)

void check(char *type, char *inst_name, uint32_t src1, uint32_t src2){
  if (src1 == src2){
    printf("[%s-Test] %s pass!\n", type, inst_name);
  }
  else{
    printf("[%s-Test] %s fail! (read 0x%x, expect 0x%x)\n", type, inst_name, src1, src2);
    assert(0);
  }
}

// U-Type Test
static const uint32_t u_inst [] = {
  0x0000b437,  // lui x8, 11
  0x00100073,  // ebreak (used as nemu_trap)
  0xdeadbeef,  // some data
};

void u_test(){
  printf("\n[U-Type Test] Start\n");
  INIT_TEST(u_inst);

  // lui
  cpu_exec(1);
  check("U", "LUI", cpu.gpr[8], (11<<12));
  // auipc has been implimented
  printf("[U-Type Test] End\n");
}

// I-Type Test
static const uint32_t i_inst [] = {
  0x00b48413,   // addi x8, x9, 
  0xfff4a413,   // slti x8, x9, -1
  0xfff4b413,   // sltiu x8, x9, -1
  0xfff4c413,   // xori x8, x9, -1
  0xfff4f413,   // andi x8, x9, -1
  0x00549413,   // slli x8, x9, 5
  0x0054d413,   // srli x8, x9, 5
  0x4054d413,   // srai x8, x9, 5
  0x10048403,   // lb x8, 256(x9)
  0x10049403,   // lh x8, 256(x9)
  0x1004a403,   // lw x8, 256(x9)
  0x1004d403,   // lhu x8, 256(x9)
  0x00100073,   // ebreak (used as nemu_trap)
  0xdeadbeef,   // some data
};
void i_test(){
  printf("\n[I-Type Test] Start\n");
  INIT_TEST(i_inst);

  // addi
  cpu.gpr[9] = 1;
  cpu_exec(1);
  check("I", "ADDI", cpu.gpr[8], 12);

  // slti
  cpu.gpr[9] = 0;
  cpu_exec(1);
  check("I", "SLTI", cpu.gpr[8], 0);

  // sltiu
  cpu.gpr[9] = 0; cpu_exec(1);
  check("I", "SLTIU", cpu.gpr[8], 1);

  // xori
  cpu.gpr[9] = 100; cpu_exec(1);
  check("I", "XORI", cpu.gpr[8], (0x64 ^ 0xFFFFFFFF));

  // andi
  cpu.gpr[9] = 0x72; cpu_exec(1);
  check("I", "ANDI", cpu.gpr[8], (0x72 & 0xFFFFFFFF));

  // slli
  cpu.gpr[9] = 0x64; cpu_exec(1);
  check("I", "SLLI", cpu.gpr[8], (0x64 << 5));

  // srli
  cpu.gpr[9] = 0xFFFFFFFF; cpu_exec(1);
  check("I", "SRLI", cpu.gpr[8], ((uint32_t)0xFFFFFFFF >> 5));

  // srai
  cpu.gpr[9] = 0xFFFFFFFF; cpu_exec(1);
  check("I", "SRAI", cpu.gpr[8], ((int32_t)0xFFFFFFFF >> 5));

  paddr_write(0x80000100, 4, 0xFFFFFFFF);
  cpu.gpr[9] = 0x80000000;
  // lb
  cpu_exec(1);
  check("I", "LB", cpu.gpr[8], 0xFFFFFFFF);

  // lh
  cpu_exec(1);
  check("I", "LH", cpu.gpr[8], 0xFFFFFFFF);

  // lw
  cpu_exec(1);
  check("I", "LW", cpu.gpr[8], 0xFFFFFFFF);

  // lbu has been tested in dummy.c
  // lhu
  cpu_exec(1);
  check("I", "LHU", cpu.gpr[8], 0xFFFF);

  printf("[I-Type Test] End\n");
}

// B-Type Test
static const uint32_t b_inst [] = {
  0x00940663,   // beq x8, x9, 12
  0x00941663,   // bne x8, x9, 12
  0x00946663,   // bltu x8, x9, 12
};

void b_test(){
  printf("\n[B-Type Test] Start\n");
  INIT_TEST(b_inst);

  uint32_t ori_pc = cpu.pc;
  // beq
  cpu.gpr[8] = 0; cpu.gpr[9] = 1; cpu_exec(1);
  check("B", "BEQ_NE", cpu.pc, ori_pc + 4);

  cpu.pc = ori_pc;
  cpu.gpr[8] = 0; cpu.gpr[9] = 0; cpu_exec(1);
  check("B", "BEQ_EQ", cpu.pc, ori_pc + 12);

  ori_pc += 4;
  // bne
  cpu.pc = ori_pc;
  cpu.gpr[8] = 0; cpu.gpr[9] = 1; cpu_exec(1);
  check("B", "BNE_NE", cpu.pc, ori_pc + 12);

  cpu.pc = ori_pc;
  cpu.gpr[8] = 0; cpu.gpr[9] = 0; cpu_exec(1);
  check("B", "BNE_EQ", cpu.pc, ori_pc + 4);

  ori_pc+=4;
  // bltu
  cpu.pc = ori_pc;
  cpu.gpr[8] = 0; cpu.gpr[9] = 0xFFFFFFFF; cpu_exec(1);
  check("B", "BLTU_LT", cpu.pc, ori_pc + 12);

  cpu.pc = ori_pc;
  cpu.gpr[8] = 0xFFFFFFFF; cpu.gpr[9] = 0; cpu_exec(1);
  check("B", "BLTU_GE", cpu.pc, ori_pc + 4);
}

// R-Type Test
static const uint32_t r_inst [] = {
  0x01248433,   // add x8, x9, x18
};

void r_test(){
  printf("\n[R-Type Test] Start\n");
  INIT_TEST(r_inst);

  // add
  cpu.gpr[18] = 1;  cpu.gpr[9] = 2; cpu_exec(1);
  check("R", "ADD", cpu.gpr[8], 3);
}
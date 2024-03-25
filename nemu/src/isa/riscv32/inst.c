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
#include "debug.h"
#include "isa.h"
#include "local-include/reg.h"
#include "macro.h"
#include "utils.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>

#define R(i) gpr(i)
#define Mr vaddr_read
#define Mw vaddr_write

enum {
  TYPE_I, TYPE_U, TYPE_S, TYPE_J, TYPE_R, TYPE_B,
  TYPE_N, // none
};

#define src1R() do { *src1 = R(rs1); } while (0)
#define src2R() do { *src2 = R(rs2); } while (0)
#define immI() do { *imm = SEXT(BITS(i, 31, 20), 12); } while(0)
#define immU() do { *imm = SEXT(BITS(i, 31, 12), 20) << 12; } while(0)
#define immS() do { *imm = (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7); } while(0)

// imm[20|10:1|11|19:12], J-Type
#define immJ() do { *imm = (SEXT(BITS(i, 31, 31), 1) << 20) | (BITS(i, 30, 21) << 1) | (BITS(i, 20, 20) << 11) | (BITS(i, 19, 12) << 12); } while(0)

// imm[12|10:5] [rs2, rs1, funct3] imm[4:1|11], B-Type
#define immB() do { *imm = (SEXT(BITS(i, 31, 31), 1) << 12) | (BITS(i, 30, 25) << 5) | (BITS(i, 11, 8) << 1) | (BITS(i, 7, 7) << 11);} while(0)

static void decode_operand(Decode *s, int *rd, word_t *src1, word_t *src2, word_t *imm, word_t *shamt, int type) {
  uint32_t i = s->isa.inst.val;
  int rs1 = BITS(i, 19, 15);
  int rs2 = BITS(i, 24, 20);
  *rd     = BITS(i, 11, 7);
  *shamt  = BITS(i, 24, 20);
  switch (type) {
    case TYPE_I: src1R();          immI(); break;
    case TYPE_U:                   immU(); break;
    case TYPE_S: src1R(); src2R(); immS(); break;
    case TYPE_R: src1R(); src2R();         break;
    case TYPE_J: immJ();                   break;
    case TYPE_B: src1R(); src2R(); immB(); break;
  }
  //Log("pc:0x%x, src1:0x%x, src2:0x%x, imm:0x%x", s->pc, *src1, *src2, *imm);
}

void tracer(uint32_t pc, int rd, uint32_t val){
  //if (CONFIG_TRACE == 1)
    //printf("COMMIT pc:0x%x, dest:%s, val:0x%x\n", pc, reg_name(rd), val);
}

// ftrace
static int fdepth = 0;
char flogbuf[100000];
void ftrace(word_t pc, word_t tar_pc, int rd, word_t val){
  int isret = 0;
  if (rd == 0){ // ret
    isret = 1;
  }

  char *buf = flogbuf;
  buf += snprintf(buf, sizeof(flogbuf), "0x%x ", pc);
  // call
  if (!isret){
    for (int i = 0; i < funcs_cnt; i++){
      if (funcs[i].entry_point == tar_pc){
        for (int j = 0; j < fdepth; j++){
          buf += snprintf(buf, sizeof(flogbuf), "\t");
        }
        fdepth++;
        buf += snprintf(buf, sizeof(flogbuf), " call %s (0x%x)", funcs[i].name, tar_pc);
        log_write("[ftrace] %s\n", flogbuf);
        return;
      }
    }
    assert(0);
  }
  else{
    for (int i = 0; i < funcs_cnt; i++){
      if (tar_pc >= funcs[i].entry_point && tar_pc <= funcs[i].entry_point + funcs[i].size){
        for (int j = 0; j < fdepth; j++){
          buf += snprintf(buf, sizeof(flogbuf), "\t");
        }
        fdepth--;
        buf += snprintf(buf, sizeof(flogbuf), " ret %s (0x%x)", funcs[i].name, tar_pc);
        log_write("[ftrace] %s\n", flogbuf);
        return;
      }
    }
    assert(0);
  }
}

static int decode_exec(Decode *s) {
  int rd = 0;
  word_t src1 = 0, src2 = 0, imm = 0, shamt  = 0;
  s->dnpc = s->snpc;

#define INSTPAT_INST(s) ((s)->isa.inst.val)
#define INSTPAT_MATCH(s, name, type, ... /* execute body */ ) { \
  decode_operand(s, &rd, &src1, &src2, &imm, &shamt, concat(TYPE_, type)); \
  __VA_ARGS__ ; \
}

  INSTPAT_START();
  // U-Type
  INSTPAT("??????? ????? ????? ??? ????? 01101 11", lui    , U, {R(rd) = imm; tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? ??? ????? 00101 11", auipc  , U, {R(rd) = s->pc + imm; tracer(s->pc, rd, R(rd));});

  // I-Type
  INSTPAT("??????? ????? ????? 000 ????? 00000 11", lb     , I, {R(rd) = SEXT(BITS(Mr(src1 + imm, 1), 7, 0), 8); tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 001 ????? 00000 11", lh     , I, {R(rd) = SEXT(BITS(Mr(src1 + imm, 2), 15, 0), 16); tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 010 ????? 00000 11", lw     , I, {R(rd) = Mr(src1 + imm, 4); tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 100 ????? 00000 11", lbu    , I, {R(rd) = Mr(src1 + imm, 1); tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 101 ????? 00000 11", lhu    , I, {R(rd) = Mr(src1 + imm, 2); tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 000 ????? 00100 11", addi   , I, {R(rd) = src1 + imm; tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 010 ????? 00100 11", slti   , I, {R(rd) = ((int32_t)src1 < (int32_t)imm); tracer(s->pc, rd, R(rd));});    // signed!
  INSTPAT("??????? ????? ????? 011 ????? 00100 11", sltiu  , I, {R(rd) = (src1 < imm); tracer(s->pc, rd, R(rd));});                      // unsigned
  INSTPAT("??????? ????? ????? 100 ????? 00100 11", xori   , I, {R(rd) = src1 ^ imm; tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 110 ????? 00100 11", ori    , I, {R(rd) = src1 | imm; tracer(s->pc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 111 ????? 00100 11", andi   , I, {R(rd) = src1 & imm; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 001 ????? 00100 11", slli   , I, {R(rd) = src1 << shamt; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 101 ????? 00100 11", srli   , I, {R(rd) = src1 >> shamt; tracer(s->pc, rd, R(rd));});
  INSTPAT("0100000 ????? ????? 101 ????? 00100 11", srai   , I, {R(rd) = ((int32_t)src1 >> shamt); tracer(s->pc, rd, R(rd));});

  // R-Type
  INSTPAT("0000000 ????? ????? 000 ????? 01100 11", add    , R, {R(rd) = src1 + src2; tracer(s->pc, rd, R(rd));});
  INSTPAT("0100000 ????? ????? 000 ????? 01100 11", sub    , R, {R(rd) = src1 - src2; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 001 ????? 01100 11", sll    , R, {R(rd) = (src1 << src2); tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 010 ????? 01100 11", slt    , R, {R(rd) = ((int32_t)src1 < (int32_t)src2); tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 011 ????? 01100 11", sltu   , R, {R(rd) = (src1 < src2); tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 100 ????? 01100 11", xor    , R, {R(rd) = src1 ^ src2; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 101 ????? 01100 11", srl    , R, {R(rd) = (src1 >> src2); tracer(s->pc, rd, R(rd));});
  INSTPAT("0100000 ????? ????? 101 ????? 01100 11", sra    , R, {R(rd) = ((int32_t)src1 >> src2); tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 110 ????? 01100 11", or     , R, {R(rd) = (src1 | src2); tracer(s->pc, rd, R(rd));});
  INSTPAT("0000000 ????? ????? 111 ????? 01100 11", and    , R, {R(rd) = (src1 & src2); tracer(s->pc, rd, R(rd));});

  // S-Type
  INSTPAT("??????? ????? ????? 000 ????? 01000 11", sb     , S, Mw(src1 + imm, 1, src2));
  INSTPAT("??????? ????? ????? 001 ????? 01000 11", sh     , S, Mw(src1 + imm, 2, src2));
  INSTPAT("??????? ????? ????? 010 ????? 01000 11", sw     , S, Mw(src1 + imm, 4, src2));
  
  // J-Type
  INSTPAT("??????? ????? ????? ??? ????? 11011 11", jal    , J, {R(rd) = s->pc + 4; s->dnpc = s->pc + imm; tracer(s->pc, rd, R(rd)); ftrace(s->pc, s->dnpc, rd, R(rd));});
  INSTPAT("??????? ????? ????? 000 ????? 11001 11", jalr   , I, {s->dnpc = (src1 + imm)&(~1); R(rd) = s->pc + 4; tracer(s->pc, rd, R(rd)); ftrace(s->pc, s->dnpc, rd, R(rd));});

  // B-Type
  INSTPAT("??????? ????? ????? 000 ????? 11000 11", beq    , B, {if ((int32_t)src1 == (int32_t)src2) s->dnpc = s->pc + imm;});
  INSTPAT("??????? ????? ????? 001 ????? 11000 11", bne    , B, {if ((int32_t)src1 != (int32_t)src2) s->dnpc = s->pc + imm;});
  INSTPAT("??????? ????? ????? 100 ????? 11000 11", blt    , B, {if ((int32_t)src1 < (int32_t)src2) s->dnpc = s->pc + imm;});
  INSTPAT("??????? ????? ????? 101 ????? 11000 11", bge    , B, {if ((int32_t)src1 >= (int32_t)src2) s->dnpc = s->pc + imm;});
  INSTPAT("??????? ????? ????? 110 ????? 11000 11", bltu   , B, {if (src1 < src2) s->dnpc = s->pc + imm;});
  INSTPAT("??????? ????? ????? 111 ????? 11000 11", bgeu   , B, {if (src1 >= src2) s->dnpc = s->pc + imm;});

  // RV32M (R-Type?)
  INSTPAT("0000001 ????? ????? 100 ????? 01100 11", div    , R, {R(rd) = (int32_t)src1 / (int32_t)src2; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000001 ????? ????? 000 ????? 01100 11", mul    , R, {R(rd) = ((int64_t)(int32_t)src1 * (int64_t)(int32_t)src2) & 0xFFFFFFFF; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000001 ????? ????? 110 ????? 01100 11", rem    , R, {R(rd) = (int32_t)src1 % (int32_t)src2; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000001 ????? ????? 001 ????? 01100 11", mulh   , R, {R(rd) = (((int64_t)(int32_t)src1 * (int64_t)(int32_t)src2) >> 32) & 0xFFFFFFFF; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000001 ????? ????? 111 ????? 01100 11", remu   , R, {R(rd) = src1 % src2; tracer(s->pc, rd, R(rd));});
  INSTPAT("0000001 ????? ????? 101 ????? 01100 11", divu   , R, {R(rd) = src1 / src2; tracer(s->pc, rd, R(rd));});

  INSTPAT("0000000 00001 00000 000 00000 11100 11", ebreak , N, NEMUTRAP(s->pc, R(10))); // R(10) is $a0
  INSTPAT("??????? ????? ????? ??? ????? ????? ??", inv    , N, INV(s->pc));
  INSTPAT_END();

  R(0) = 0; // reset $zero to 0

  return 0;
}

int isa_exec_once(Decode *s) {
  s->isa.inst.val = inst_fetch(&s->snpc, 4);

  return decode_exec(s);
}

#include "isa.h"
#include "common.h"
#include "cpu.h"
#include "VysyxSoCFull.h"
#include "verilated.h"
#include "VysyxSoCFull___024root.h"
#include <cassert>

rv32_CPU_state CPU_state;
const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

// Get RegFile Value
uint32_t RTL_REGFILE(int index) {
    assert(index < REG_NUM);
    switch (index) {
        case 0: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_0;
        case 1: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_1;
        case 2: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_2;
        case 3: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_3;
        case 4: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_4;
        case 5: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_5;
        case 6: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_6;
        case 7: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_7;
        case 8: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_8;
        case 9: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_9;
        case 10: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_10;
        case 11: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_11;
        case 12: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_12;
        case 13: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_13;
        case 14: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_14;
        case 15: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_15;
        case 16: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_16;
        case 17: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_17;
        case 18: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_18;
        case 19: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_19;
        case 20: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_20;
        case 21: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_21;
        case 22: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_22;
        case 23: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_23;
        case 24: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_24;
        case 25: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_25;
        case 26: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_26;
        case 27: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_27;
        case 28: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_28;
        case 29: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_29;
        case 30: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_30;
        case 31: return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__regfile__DOT__RegArray_31;
        default: return -1; // 或者您可以根据需求返回其他值或抛出错误
    }
}

// Get PC value
uint32_t RTL_PC(){
    return get_commit_pc(dut);
}

// update CPU state from RTL (npc)
void update_npcstate(){
    for (int i = 0; i < REG_NUM; i++)
        CPU_state.gpr[i] = RTL_REGFILE(i);
    CPU_state.pc = RTL_PC();
}

char *get_regname(int index){
    return (char *)regs[index];
}
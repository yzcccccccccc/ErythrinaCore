#include "common.h"
#include <bits/types/time_t.h>
#include <cstdint>
#include <ctime>
#include <perf.h>

#include <cstdio>
#include <cpu.h>

#ifdef __SOC__
#include "VysyxSoCFull.h"
#include "VysyxSoCFull___024root.h"
#endif

struct perf_t{
    uint64_t cycles;
    uint64_t instrs;

    uint64_t get_instr_event;
    uint64_t inst_req_wait;
    uint64_t inst_resp_wait;

    uint64_t cal_instrs;
    uint64_t csr_instrs;
    uint64_t ld_instrs;
    uint64_t st_instrs;
    uint64_t j_instrs;
    uint64_t b_instrs;
    uint64_t pause_event;

    uint64_t ld_data_event;
    uint64_t st_data_event;
    uint64_t data_req_wait;
    uint64_t data_resp_wait;

    uint64_t bpu_hit_event;
    uint64_t bpu_miss_event;
    uint64_t bpu_kick_event[64];

    uint64_t icache_hit_event;
    uint64_t icache_miss_event;
    uint64_t icache_bypass_event;
} perf_cnt;

// get data from dut
void perf_get_bpu_kick(){
#ifdef __SOC__
    perf_cnt.bpu_kick_event[0]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_0;
    perf_cnt.bpu_kick_event[1]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_1;
    perf_cnt.bpu_kick_event[2]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_2;
    perf_cnt.bpu_kick_event[3]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_3;
    perf_cnt.bpu_kick_event[4]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_4;
    perf_cnt.bpu_kick_event[5]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_5;
    perf_cnt.bpu_kick_event[6]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_6;
    perf_cnt.bpu_kick_event[7]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_7;
    perf_cnt.bpu_kick_event[8]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_8;
    perf_cnt.bpu_kick_event[9]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_9;
    perf_cnt.bpu_kick_event[10]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_10;
    perf_cnt.bpu_kick_event[11]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_11;
    perf_cnt.bpu_kick_event[12]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_12;
    perf_cnt.bpu_kick_event[13]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_13;
    perf_cnt.bpu_kick_event[14]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_14;
    perf_cnt.bpu_kick_event[15]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_15;
    perf_cnt.bpu_kick_event[16]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_16;
    perf_cnt.bpu_kick_event[17]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_17;
    perf_cnt.bpu_kick_event[18]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_18;
    perf_cnt.bpu_kick_event[19]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_19;
    perf_cnt.bpu_kick_event[20]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_20;
    perf_cnt.bpu_kick_event[21]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_21;
    perf_cnt.bpu_kick_event[22]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_22;
    perf_cnt.bpu_kick_event[23]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_23;
    perf_cnt.bpu_kick_event[24]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_24;
    perf_cnt.bpu_kick_event[25]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_25;
    perf_cnt.bpu_kick_event[26]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_26;
    perf_cnt.bpu_kick_event[27]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_27;
    perf_cnt.bpu_kick_event[28]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_28;
    perf_cnt.bpu_kick_event[29]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_29;
    perf_cnt.bpu_kick_event[30]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_30;
    perf_cnt.bpu_kick_event[31]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_31;
    perf_cnt.bpu_kick_event[32]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_32;
    perf_cnt.bpu_kick_event[33]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_33;
    perf_cnt.bpu_kick_event[34]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_34;
    perf_cnt.bpu_kick_event[35]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_35;
    perf_cnt.bpu_kick_event[36]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_36;
    perf_cnt.bpu_kick_event[37]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_37;
    perf_cnt.bpu_kick_event[38]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_38;
    perf_cnt.bpu_kick_event[39]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_39;
    perf_cnt.bpu_kick_event[40]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_40;
    perf_cnt.bpu_kick_event[41]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_41;
    perf_cnt.bpu_kick_event[42]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_42;
    perf_cnt.bpu_kick_event[43]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_43;
    perf_cnt.bpu_kick_event[44]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_44;
    perf_cnt.bpu_kick_event[45]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_45;
    perf_cnt.bpu_kick_event[46]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_46;
    perf_cnt.bpu_kick_event[47]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_47;
    perf_cnt.bpu_kick_event[48]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_48;
    perf_cnt.bpu_kick_event[49]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_49;
    perf_cnt.bpu_kick_event[50]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_50;
    perf_cnt.bpu_kick_event[51]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_51;
    perf_cnt.bpu_kick_event[52]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_52;
    perf_cnt.bpu_kick_event[53]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_53;
    perf_cnt.bpu_kick_event[54]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_54;
    perf_cnt.bpu_kick_event[55]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_55;
    perf_cnt.bpu_kick_event[56]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_56;
    perf_cnt.bpu_kick_event[57]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_57;
    perf_cnt.bpu_kick_event[58]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_58;
    perf_cnt.bpu_kick_event[59]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_59;
    perf_cnt.bpu_kick_event[60]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_60;
    perf_cnt.bpu_kick_event[61]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_61;
    perf_cnt.bpu_kick_event[62]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_62;
    perf_cnt.bpu_kick_event[63]  = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_kick_63;
#endif
}

void perf_res_get(){
#ifdef __SOC__
    perf_get_bpu_kick();
    perf_cnt.cycles = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_cycles;
    perf_cnt.instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_insts;

    // Instruction Event
    perf_cnt.get_instr_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_get_insts;
    perf_cnt.inst_req_wait = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_ifu_wait_req;
    perf_cnt.inst_resp_wait = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_ifu_wait_resp;

    // Calculate Event
    perf_cnt.cal_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_cal_insts;
    perf_cnt.csr_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_csr_insts;
    perf_cnt.ld_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_ld_insts;
    perf_cnt.st_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_st_insts;
    perf_cnt.j_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_j_insts;
    perf_cnt.b_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_b_insts;

    // IDU pause
    perf_cnt.pause_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_pause_event;

    // Data Event
    perf_cnt.ld_data_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_ld_data_events;
    perf_cnt.st_data_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_st_data_events;
    perf_cnt.data_req_wait = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_memu_wait_req;
    perf_cnt.data_resp_wait = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_memu_wait_resp;

    // BPU Event
    perf_cnt.bpu_hit_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_hit;
    perf_cnt.bpu_miss_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_miss;

    // icache Event
    perf_cnt.icache_hit_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_icache_hit;
    perf_cnt.icache_miss_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_icache_miss;
    perf_cnt.icache_bypass_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_icache_bypass;
#endif
}

void perf_res_show(){
    // print the performance res
    printf("\n%s====================== Performance Counter ======================%s\n", FontBlue, Restore);
#ifdef __SOC__
    perf_res_get();
    printf("\tCycles: \t\t\t\t%ld\n", perf_cnt.cycles);
    printf("\tInstrs: \t\t\t\t%ld\n", perf_cnt.instrs );
    printf("\tIPC = \t\t\t\t\t%.10lf\n", (double)perf_cnt.instrs / perf_cnt.cycles);
    printf("\n");
    printf("\tGet Instr Event: \t\t\t%ld\n", perf_cnt.get_instr_event);
    printf("\tInst Req Wait: \t\t\t\t%ld\n", perf_cnt.inst_req_wait);
    printf("\tInst Resp Wait: \t\t\t%ld\n", perf_cnt.inst_resp_wait);
    printf("\tInst Total Delay: \t\t\t%ld\n", perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait);
    printf("\tInst Average Delay: \t\t\t%.10lf\n", (double)(perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait) / perf_cnt.get_instr_event);
    printf("\tInst-Mem-Delay Per Inst: \t\t%.10lf\n", (double)(perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait) / perf_cnt.instrs);
    printf("\n");
    printf("\tCal Instrs: \t\t\t\t%ld\n", perf_cnt.cal_instrs);
    printf("\tCSR Instrs: \t\t\t\t%ld\n", perf_cnt.csr_instrs);
    printf("\tLD Instrs: \t\t\t\t%ld\n", perf_cnt.ld_instrs);
    printf("\tST Instrs: \t\t\t\t%ld\n", perf_cnt.st_instrs);
    printf("\tJ Instrs: \t\t\t\t%ld\n", perf_cnt.j_instrs);
    printf("\tB Instrs: \t\t\t\t%ld\n", perf_cnt.b_instrs);
    printf("\tPause Event: \t\t\t\t%ld\n", perf_cnt.pause_event);
    printf("\n");
    printf("\tLD Data Event: \t\t\t\t%ld\n", perf_cnt.ld_data_event);
    printf("\tST Data Event: \t\t\t\t%ld\n", perf_cnt.st_data_event);
    printf("\tData Req Wait: \t\t\t\t%ld\n", perf_cnt.data_req_wait);
    printf("\tData Resp Wait: \t\t\t%ld\n", perf_cnt.data_resp_wait);
    printf("\tData Total Delay: \t\t\t%ld\n", perf_cnt.data_req_wait + perf_cnt.data_resp_wait);
    printf("\tData Average Delay: \t\t\t%.10lf\n", (double)(perf_cnt.data_req_wait + perf_cnt.data_resp_wait) / (perf_cnt.ld_data_event + perf_cnt.st_data_event));
    printf("\tData-Mem-Delay Per Inst: \t\t%.10lf\n", (double)(perf_cnt.data_req_wait + perf_cnt.data_resp_wait) / perf_cnt.instrs);
    printf("BPU\n");
    printf("\tBPU Hit Event: \t\t\t\t%ld\n", perf_cnt.bpu_hit_event);
    printf("\tBPU Miss Event: \t\t\t%ld\n", perf_cnt.bpu_miss_event);
    printf("BPU kick\n");
    for(int i = 0; i < 64; i++){
        printf("\tBPU Kick Event[%d]: \t\t\t%ld\n", i, perf_cnt.bpu_kick_event[i]);
    }
    printf("icache\n");
    printf("\tICache Hit Event: \t\t\t%ld\n", perf_cnt.icache_hit_event);
    printf("\tICache Miss Event: \t\t\t%ld\n", perf_cnt.icache_miss_event);
    printf("\tICache Bypass Event: \t\t\t%ld\n", perf_cnt.icache_bypass_event);
#else
    printf("\t%sOnly support __SOC__%s\n", FontYellow, Restore);
#endif
    printf("%s=================================================================%s\n", FontBlue, Restore);
}

// Record the performance res to perf_log
void perf_res_record(){
    fprintf(perf_log, "Performance Counter Result\n");
    
    time_t cur_time;
    time(&cur_time);
    struct tm *local_time = localtime(&cur_time);
    fprintf(perf_log, "Test Time: %s", asctime(local_time)); 
    fprintf(perf_log, "------------------------------------------------------------\n");
#ifdef __SOC__
    fprintf(perf_log, "\tCycles: \t\t\t\t\t%ld\n", perf_cnt.cycles);
    fprintf(perf_log, "\tInstrs: \t\t\t\t\t%ld\n", perf_cnt.instrs );
    fprintf(perf_log, "\tIPC = \t\t\t\t\t\t%.10lf\n", (double)perf_cnt.instrs / perf_cnt.cycles);
    fprintf(perf_log, "\n");
    fprintf(perf_log, "\tGet Instr Event: \t\t\t%ld\n", perf_cnt.get_instr_event);
    fprintf(perf_log, "\tInst Req Wait: \t\t\t\t%ld\n", perf_cnt.inst_req_wait);
    fprintf(perf_log, "\tInst Resp Wait: \t\t\t%ld\n", perf_cnt.inst_resp_wait);
    fprintf(perf_log, "\tInst Total Delay: \t\t\t%ld\n", perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait);
    fprintf(perf_log, "\tInst Average Delay: \t\t%.10lf\n", (double)(perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait) / perf_cnt.get_instr_event);
    fprintf(perf_log, "\tInst-Mem-Delay Per Inst: \t%.10lf\n", (double)(perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait) / perf_cnt.instrs);
    fprintf(perf_log, "\n");
    fprintf(perf_log, "\tCAL Instrs: \t\t\t\t%ld\n", perf_cnt.cal_instrs);
    fprintf(perf_log, "\tCSR Instrs: \t\t\t\t%ld\n", perf_cnt.csr_instrs);
    fprintf(perf_log, "\tLD Instrs: \t\t\t\t\t%ld\n", perf_cnt.ld_instrs);
    fprintf(perf_log, "\tST Instrs: \t\t\t\t\t%ld\n", perf_cnt.st_instrs);
    fprintf(perf_log, "\tJ Instrs: \t\t\t\t\t%ld\n", perf_cnt.j_instrs);
    fprintf(perf_log, "\tB Instrs: \t\t\t\t\t%ld\n", perf_cnt.b_instrs);
    fprintf(perf_log, "\tPause Event: \t\t\t\t%ld\n", perf_cnt.pause_event);
    fprintf(perf_log, "\n");
    fprintf(perf_log, "\tLD Data Event: \t\t\t\t%ld\n", perf_cnt.ld_data_event);
    fprintf(perf_log, "\tST Data Event: \t\t\t\t%ld\n", perf_cnt.st_data_event);
    fprintf(perf_log, "\tData Req Wait: \t\t\t\t%ld\n", perf_cnt.data_req_wait);
    fprintf(perf_log, "\tData Resp Wait: \t\t\t%ld\n", perf_cnt.data_resp_wait);
    fprintf(perf_log, "\tData Total Delay: \t\t\t%ld\n", perf_cnt.data_req_wait + perf_cnt.data_resp_wait);
    fprintf(perf_log, "\tData Average Delay: \t\t%.10lf\n", (double)(perf_cnt.data_req_wait + perf_cnt.data_resp_wait) / (perf_cnt.ld_data_event + perf_cnt.st_data_event));
    fprintf(perf_log, "\tData-Mem-Delay Per Inst: \t%.10lf\n", (double)(perf_cnt.data_req_wait + perf_cnt.data_resp_wait) / perf_cnt.instrs);
    fprintf(perf_log, "BPU\n");
    fprintf(perf_log, "\tBPU Hit Event: \t\t\t\t%ld\n", perf_cnt.bpu_hit_event);
    fprintf(perf_log, "\tBPU Miss Event: \t\t\t%ld\n", perf_cnt.bpu_miss_event);
    fprintf(perf_log, "BPU kick\n");
    for(int i = 0; i < 64; i++){
        fprintf(perf_log, "\tBPU Kick Event[%d]: \t\t\t%ld\n", i, perf_cnt.bpu_kick_event[i]);
    }
    fprintf(perf_log, "icache\n");
    fprintf(perf_log, "\tICache Hit Event: \t\t\t%ld\n", perf_cnt.icache_hit_event);
    fprintf(perf_log, "\tICache Miss Event: \t\t\t%ld\n", perf_cnt.icache_miss_event);
    fprintf(perf_log, "\tICache Bypass Event: \t\t%ld\n", perf_cnt.icache_bypass_event);
#endif

#ifdef __SIM__
    fprintf(perf_log, "\tOnly support __SOC__\n");
#endif
    fprintf(perf_log, "------------------------------------------------------------\n");
    printf("%s Result has been recorded in build/report/perf.log %s\n", FontBlue, Restore);
}
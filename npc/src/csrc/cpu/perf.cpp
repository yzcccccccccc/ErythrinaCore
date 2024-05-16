#include "common.h"
#include <bits/types/time_t.h>
#include <ctime>
#include <perf.h>

#include <cstdio>
#include <cpu.h>

#ifdef __SOC__
#include "VysyxSoCFull.h"
#include "VysyxSoCFull___024root.h"
#endif

struct perf_t{
    uint32_t cycles;
    uint32_t instrs;

    uint32_t get_instr_event;
    uint32_t inst_req_wait;
    uint32_t inst_resp_wait;

    uint32_t cal_instrs;
    uint32_t csr_instrs;
    uint32_t ld_instrs;
    uint32_t st_instrs;
    uint32_t j_instrs;
    uint32_t b_instrs;

    uint32_t ld_data_event;
    uint32_t st_data_event;
    uint32_t data_req_wait;
    uint32_t data_resp_wait;

    uint32_t bpu_hit_event;
    uint32_t bpu_miss_event;
} perf_cnt;

// get data from dut
void perf_res_get(){
#ifdef __SOC__
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

    // Data Event
    perf_cnt.ld_data_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_ld_data_events;
    perf_cnt.st_data_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_st_data_events;
    perf_cnt.data_req_wait = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_memu_wait_req;
    perf_cnt.data_resp_wait = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_memu_wait_resp;

    // BPU Event
    perf_cnt.bpu_hit_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_hit;
    perf_cnt.bpu_miss_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_bpu_miss;
#endif
}

void perf_res_show(){
    // print the performance res
    printf("\n%s====================== Performance Counter ======================%s\n", FontBlue, Restore);
#ifdef __SOC__
    perf_res_get();
    printf("\tCycles: \t\t\t\t%d\n", perf_cnt.cycles);
    printf("\tInstrs: \t\t\t\t%d\n", perf_cnt.instrs );
    printf("\tIPC = \t\t\t\t\t%.10lf\n", (double)perf_cnt.instrs / perf_cnt.cycles);
    printf("\n");
    printf("\tGet Instr Event: \t\t\t%d\n", perf_cnt.get_instr_event);
    printf("\tInst Req Wait: \t\t\t\t%d\n", perf_cnt.inst_req_wait);
    printf("\tInst Resp Wait: \t\t\t%d\n", perf_cnt.inst_resp_wait);
    printf("\tInst Total Delay: \t\t\t%d\n", perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait);
    printf("\tInst Average Delay: \t\t\t%.10lf\n", (double)(perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait) / perf_cnt.get_instr_event);
    printf("\n");
    printf("\tCal Instrs: \t\t\t\t%d\n", perf_cnt.cal_instrs);
    printf("\tCSR Instrs: \t\t\t\t%d\n", perf_cnt.csr_instrs);
    printf("\tLD Instrs: \t\t\t\t%d\n", perf_cnt.ld_instrs);
    printf("\tST Instrs: \t\t\t\t%d\n", perf_cnt.st_instrs);
    printf("\tJ Instrs: \t\t\t\t%d\n", perf_cnt.j_instrs);
    printf("\tB Instrs: \t\t\t\t%d\n", perf_cnt.b_instrs);
    printf("\n");
    printf("\tLD Data Event: \t\t\t\t%d\n", perf_cnt.ld_data_event);
    printf("\tST Data Event: \t\t\t\t%d\n", perf_cnt.st_data_event);
    printf("\tData Req Wait: \t\t\t\t%d\n", perf_cnt.data_req_wait);
    printf("\tData Resp Wait: \t\t\t%d\n", perf_cnt.data_resp_wait);
    printf("\tData Total Delay: \t\t\t%d\n", perf_cnt.data_req_wait + perf_cnt.data_resp_wait);
    printf("\tData Average Delay: \t\t\t%.10lf\n", (double)(perf_cnt.data_req_wait + perf_cnt.data_resp_wait) / (perf_cnt.ld_data_event + perf_cnt.st_data_event));
    printf("\n");
    printf("\tBPU Hit Event: \t\t\t\t%d\n", perf_cnt.bpu_hit_event);
    printf("\tBPU Miss Event: \t\t\t%d\n", perf_cnt.bpu_miss_event);
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
    fprintf(perf_log, "\tCycles: \t\t\t\t\t%d\n", perf_cnt.cycles);
    fprintf(perf_log, "\tInstrs: \t\t\t\t\t%d\n", perf_cnt.instrs );
    fprintf(perf_log, "\tIPC = \t\t\t\t\t\t%.10lf\n", (double)perf_cnt.instrs / perf_cnt.cycles);
    fprintf(perf_log, "\n");
    fprintf(perf_log, "\tGet Instr Event: \t\t\t%d\n", perf_cnt.get_instr_event);
    fprintf(perf_log, "\tInst Req Wait: \t\t\t\t%d\n", perf_cnt.inst_req_wait);
    fprintf(perf_log, "\tInst Resp Wait: \t\t\t%d\n", perf_cnt.inst_resp_wait);
    fprintf(perf_log, "\tInst Total Delay: \t\t\t%d\n", perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait);
    fprintf(perf_log, "\tInst Average Delay: \t\t%.10lf\n", (double)(perf_cnt.inst_req_wait + perf_cnt.inst_resp_wait) / perf_cnt.get_instr_event);
    fprintf(perf_log, "\n");
    fprintf(perf_log, "\tCAL Instrs: \t\t\t\t%d\n", perf_cnt.cal_instrs);
    fprintf(perf_log, "\tCSR Instrs: \t\t\t\t%d\n", perf_cnt.csr_instrs);
    fprintf(perf_log, "\tLD Instrs: \t\t\t\t\t%d\n", perf_cnt.ld_instrs);
    fprintf(perf_log, "\tST Instrs: \t\t\t\t\t%d\n", perf_cnt.st_instrs);
    fprintf(perf_log, "\tJ Instrs: \t\t\t\t\t%d\n", perf_cnt.j_instrs);
    fprintf(perf_log, "\tB Instrs: \t\t\t\t\t%d\n", perf_cnt.b_instrs);
    fprintf(perf_log, "\n");
    fprintf(perf_log, "\tLD Data Event: \t\t\t\t%d\n", perf_cnt.ld_data_event);
    fprintf(perf_log, "\tST Data Event: \t\t\t\t%d\n", perf_cnt.st_data_event);
    fprintf(perf_log, "\tData Req Wait: \t\t\t\t%d\n", perf_cnt.data_req_wait);
    fprintf(perf_log, "\tData Resp Wait: \t\t\t%d\n", perf_cnt.data_resp_wait);
    fprintf(perf_log, "\tData Total Delay: \t\t\t%d\n", perf_cnt.data_req_wait + perf_cnt.data_resp_wait);
    fprintf(perf_log, "\tData Average Delay: \t\t%.10lf\n", (double)(perf_cnt.data_req_wait + perf_cnt.data_resp_wait) / (perf_cnt.ld_data_event + perf_cnt.st_data_event));
#endif

#ifdef __SIM__
    fprintf(perf_log, "\tOnly support __SOC__\n");
#endif
    fprintf(perf_log, "------------------------------------------------------------\n");
    printf("%s Result has been recorded in build/report/perf.log %s\n", FontBlue, Restore);
}
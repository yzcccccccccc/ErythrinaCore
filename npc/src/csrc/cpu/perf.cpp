#include "common.h"
#include <perf.h>

#include <cstdio>

#ifdef __SOC__
#include "cpu.h"
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

#else
    printf("\t%sOnly support __SOC__%s\n", FontYellow, Restore);
#endif
    printf("%s=================================================================%s\n", FontBlue, Restore);
}
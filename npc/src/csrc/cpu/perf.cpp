#include "common.h"
#include <perf.h>
#include <cpu.h>

#ifdef __SOC__
#include "VysyxSoCFull.h"
#include "VysyxSoCFull___024root.h"
#endif

struct perf_t{
    uint32_t cycles;
    uint32_t instrs;

    uint32_t get_instr_event;

    uint32_t cal_instrs;
    uint32_t csr_instrs;
    uint32_t ld_instrs;
    uint32_t st_instrs;
    uint32_t j_instrs;
    uint32_t b_instrs;

    uint32_t ld_data_event;
    uint32_t st_data_event;
} perf_cnt;

// get data from dut
void perf_res_get(){
#ifdef __SOC__
    perf_cnt.cycles = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_cycles;
    perf_cnt.instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_insts;

    perf_cnt.get_instr_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_get_insts;

    perf_cnt.cal_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_cal_insts;
    perf_cnt.csr_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_csr_insts;
    perf_cnt.ld_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_ld_insts;
    perf_cnt.st_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_st_insts;
    perf_cnt.j_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_j_insts;
    perf_cnt.b_instrs = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_b_insts;

    perf_cnt.ld_data_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_ld_data_events;
    perf_cnt.st_data_event = dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__erythrinacore__DOT__perfbox__DOT__total_st_data_events;
#endif
}

void perf_res_show(){
    // print the performance res
    printf("\n%s============ Performance Counter ====================%s\n", FontBlue, Restore);
#ifdef __SOC__
    perf_res_get();
    printf("Cycles: \t\t\t\t%d\n", perf_cnt.cycles);
    printf("Instrs: \t\t\t\t%d\n", perf_cnt.instrs);
    printf("Get Instr Event: \t\t\t%d\n", perf_cnt.get_instr_event);
    printf("Cal Instrs: \t\t\t\t%d\n", perf_cnt.cal_instrs);
    printf("CSR Instrs: \t\t\t\t%d\n", perf_cnt.csr_instrs);
    printf("LD Instrs: \t\t\t\t%d\n", perf_cnt.ld_instrs);
    printf("ST Instrs: \t\t\t\t%d\n", perf_cnt.st_instrs);
    printf("J Instrs: \t\t\t\t%d\n", perf_cnt.j_instrs);
    printf("B Instrs: \t\t\t\t%d\n", perf_cnt.b_instrs);
    printf("LD Data Event: \t\t\t\t%d\n", perf_cnt.ld_data_event);
    printf("ST Data Event: \t\t\t\t%d\n", perf_cnt.st_data_event);

#else
    printf("%sOnly support __SOC__%s\n", FontYellow, Restore);
#endif
    printf("%s=====================================================%s\n", FontBlue, Restore);
}
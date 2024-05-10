#include "cpu.h"
#ifdef __SOC__
#include "VysyxSoCFull__Dpi.h"
#include "VysyxSoCFull___024root.h"
#endif

#ifdef __SIM__
#include "VSimTop__Dpi.h"
#include "VSimTop___024root.h"
#endif

uint32_t get_commit_valid(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__valid_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__valid_r;
#endif
    assert(0);
}
uint32_t get_commit_pc(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__pc_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__pc_r;
#endif
    assert(0);
}
uint32_t get_commit_inst(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__inst_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__inst_r;
#endif
    assert(0);
}
uint32_t get_commit_rf_waddr(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__rf_waddr_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__rf_waddr_r;
#endif
    assert(0);
}
uint32_t get_commit_rf_wdata(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__rf_wdata_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__rf_wdata_r;
#endif
    assert(0);
}
uint32_t get_commit_rf_wen(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__rf_wen_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__rf_wen_r;
#endif
    assert(0);
}
uint32_t get_commit_mem_addr(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__mem_addr_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__mem_addr_r;
#endif
    assert(0);
}
uint32_t get_commit_mem_en(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__mem_en_r;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__mem_en_r;
#endif
    assert(0);
}
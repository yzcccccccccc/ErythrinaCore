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
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_valid;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_valid;
#endif
    assert(0);
}
uint32_t get_commit_pc(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_pc;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_pc;
#endif
    assert(0);
}
uint32_t get_commit_inst(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_inst;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_inst;
#endif
    assert(0);
}
uint32_t get_commit_rf_waddr(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_rf_waddr;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_rf_waddr;
#endif
    assert(0);
}
uint32_t get_commit_rf_wdata(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_rf_wdata;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_rf_wdata;
#endif
    assert(0);
}
uint32_t get_commit_rf_wen(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_rf_wen;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_rf_wen;
#endif
    assert(0);
}
uint32_t get_commit_mem_addr(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_mem_addr;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_mem_addr;
#endif
    assert(0);
}
uint32_t get_commit_mem_en(VSoc *dut){
#ifdef __SOC__
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__port_r_mem_en;
#endif
#ifdef __SIM__
    return dut->rootp->SimTop__DOT__commit__DOT__port_r_mem_en;
#endif
    assert(0);
}
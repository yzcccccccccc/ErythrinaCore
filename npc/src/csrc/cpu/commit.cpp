#include "cpu.h"
#include "VysyxSoCFull__Dpi.h"
#include "VysyxSoCFull___024root.h"

uint32_t get_commit_valid(VSoc *dut){
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__valid_r;
}
uint32_t get_commit_pc(VSoc *dut){
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__pc_r;
}
uint32_t get_commit_inst(VSoc *dut){
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__inst_r;
}
uint32_t get_commit_rf_waddr(VSoc *dut){
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__rf_waddr_r;
}
uint32_t get_commit_rf_wdata(VSoc *dut){
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__rf_wdata_r;
}
uint32_t get_commit_rf_wen(VSoc *dut){
    return dut->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__commit__DOT__rf_wen_r;
}
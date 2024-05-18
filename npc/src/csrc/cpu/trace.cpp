#include <trace.h>
#include <util.h>
#include <cpu.h>
#include <cstdio>
FILE *itrace_file, *mtrace_file, *irbuf_file;

#define IRINGBUF_LEN 1000
char irbuf[IRINGBUF_LEN][256];
char irbuf_ptr;
int irbuf_valid[IRINGBUF_LEN];

void itrace_init(){
    itrace_file = fopen("./build/itrace.log", "w");
    irbuf_ptr = 0;
}

char inst_disasm[100];
void itrace_record(){
    uint32_t inst   = get_commit_inst(dut);
    uint32_t pc     = get_commit_pc(dut);
    disassemble(inst_disasm, 100, pc, (uint8_t *)&(inst), 4);
    sprintf(irbuf[irbuf_ptr], "[Trace-%010ld]:\n\tPC=0x%08x,\tInst=0x%08x (%s), \n\trf_waddr=0x%x,\trf_wdata=0x%08x,\trf_wen=%d,\taddr=0x%08x,\ten=%x\n",
                instr,
                pc, inst, inst_disasm,
                get_commit_rf_waddr(dut), get_commit_rf_wdata(dut),
                get_commit_rf_wen(dut),
                get_commit_mem_addr(dut), get_commit_mem_en(dut)
            );
    irbuf_valid[irbuf_ptr] = 1;
    fprintf(itrace_file, "%s", irbuf[irbuf_ptr]);
    irbuf_ptr = (irbuf_ptr + 1) % IRINGBUF_LEN;
}

void irbuf_dump(){
    irbuf_file  = fopen("./build/irbuf.log", "w");
    for (int i = 0; i < IRINGBUF_LEN; i++){
        if (irbuf_valid[i]){
            fprintf(irbuf_file, "------------------------------------------------------------------------------------\n");
            fprintf(irbuf_file, "%s%s", (i == irbuf_ptr - 1 ? "->" : "  "), irbuf[i]);
        }
    }
    fprintf(irbuf_file, "------------------------------------------------------------------------------------\n");
    fclose(irbuf_file);
}

void mtrace_init(){
    mtrace_file = fopen("./build/mtrace.log", "w");
}

void mtrace_record(){
    uint32_t en = get_commit_mem_en(dut);
    uint32_t addr = get_commit_mem_addr(dut);
    if (en){
        fprintf(mtrace_file, "[mtrace] r/w at 0x%08x\n", addr);
    }
}
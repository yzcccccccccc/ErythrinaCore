/*
    Replay the wrong point
    Author: yzcc
*/

#include "cpu.h"
#include "replay.h"
#include "setting.h"
#include "verilated_fst_c.h"
#include <cstdio>
#include <verilated.h>
#include <cstdint>
#include <verilated_save.h>

char chkpoint1[] = "chkpoint1.dat";
char chkpoint2[] = "chkpoint2.dat";
uint64_t chkp1_cycle = 0, chkp2_cycle = 0;

void save_model(const char *filenamep){
    VerilatedSave os;
    os.open(filenamep);
    
    // save the time stamp
    os << contx->time();

    // save the module
    os << *dut;
}

void retore_model(const char *filenamep){
    VerilatedRestore os;
    os.open(filenamep);

    // restore the time stamp
    uint64_t tsp;
    os >> tsp;
    contx->time(tsp);

    // restore the module
    os >> *dut;
}

void add_chk(uint64_t cycle){
    if (USE_REPLAY & DUMP_WAVE){
        if (cycle % REPLAY_INTERVAL == 0){
            save_model(chkpoint1);
            chkp1_cycle = cycle;
        }
        if (cycle % REPLAY_INTERVAL != 0 & cycle % (REPLAY_INTERVAL / 2) == 0 | cycle == 0){
            save_model(chkpoint2);
            chkp2_cycle = cycle;
        }
    }
}

// replay the wrong point!
void replay_1_cycle(){
    half_cycle(dut, tfp, contx);
    half_cycle(dut, tfp, contx);
}

void replay(uint64_t wrong_point){
    printf("---------------------------------------------------------------------------\n");
    // Run the simulation
    trace_is_on = true;
    
    // open trace
    tfp = new VerilatedFstC;
    contx->traceEverOn(true);
    dut->trace(tfp, 1);
    tfp->open("wavefile");

    // Restore Check Point
    if (wrong_point % REPLAY_INTERVAL > (REPLAY_INTERVAL / 2)){
        retore_model(chkpoint1);   
        cycle = chkp1_cycle;
    }
    else{
        retore_model(chkpoint2);
        cycle = chkp2_cycle;
    }
    npc_state = CPU_RUN;

    printf("[Info] Replay the point from cycle %ld.\n", cycle);
    while (npc_state == CPU_RUN & cycle <= wrong_point){
        replay_1_cycle();
        cycle++;
    }
    printf("[Info] Replay finished at cycle %ld (target point: %ld).\n", cycle, wrong_point);
    printf("---------------------------------------------------------------------------\n");
}
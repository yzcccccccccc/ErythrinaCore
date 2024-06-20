/*
    Replay the wrong point
    Author: yzcc
*/

#include "cpu.h"
#include "replay.h"
#include "setting.h"
#include <cstdio>
#include <verilated.h>
#include <cstdint>
#include <verilated_save.h>

char chkpoint1[] = "chkpoint1.dat";
char chkpoint2[] = "chkpoint2.dat";

void save_model(const char *filenamep){
    VerilatedSave os;
    os.open(filenamep);
    
    // save the time stamp
    os << contx->time();

    // save the module
    os << *dut;
}

void retore_model(const char *filenamep){
    // open new context
    contx = new VerilatedContext;

    VerilatedRestore os;
    os.open(filenamep);

    // restore the time stamp
    uint64_t main_time;
    os >> main_time;
    contx->time(main_time);

    // restore the module
    os >> *dut;
}

void add_chk(uint64_t cycle){
    if (USE_REPLAY){
        if (cycle % REPLAY_INTERVAL == 0){
            save_model(chkpoint1);
        }
        if (cycle % REPLAY_INTERVAL != 0 & cycle % (REPLAY_INTERVAL / 2) == 0){
            save_model(chkpoint2);
        }
    }
}

// replay the wrong point!
void replay_1_cycle(){
    half_cycle(dut, tfp, contx);
    half_cycle(dut, tfp, contx);
}

void replay(uint64_t wrong_point){
    printf("---------------------------------------------------------------------------");
    printf("[Info] Replay the wrong point at cycle %ld.\n", wrong_point);
    // Restore Check Point
    if (wrong_point % REPLAY_INTERVAL > (REPLAY_INTERVAL / 2)){
        retore_model(chkpoint1);   
        cycle = (wrong_point / REPLAY_INTERVAL) * REPLAY_INTERVAL;
    }
    else{
        retore_model(chkpoint2);
        cycle = (wrong_point / REPLAY_INTERVAL) * REPLAY_INTERVAL + REPLAY_INTERVAL / 2;
    }
    npc_state = CPU_RUN;

    // Run the simulation
    trace_is_on = true;
    init_wave_dmp();
    while (npc_state == CPU_RUN & cycle <= wrong_point){
        replay_1_cycle();
        cycle++;
    }
    printf("[Info] Replay finished at cycle %ld.\n", cycle);
    printf("---------------------------------------------------------------------------");
}
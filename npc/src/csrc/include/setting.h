#ifndef __SETTING_H__
#define __SETTING_H__


// Wave
#define DUMP_WAVE       1
    // TBD
#define USE_WINDOW      0
#define WINDOW_SIZE     100000
#define WINDOW_BOUND    64982175                // will record [WINDOW_BOUND - WINDOW_SIZE, WINDOW_BOUND] cycles

#define CYCLE_BOUND -1
#define INSTR_BOUND -1
#define DIFF_TEST 1

#define ITRACE 1
#define MTRACE 1
#define DTRACE 0

#define __SIM__
//#define __SOC__

#endif
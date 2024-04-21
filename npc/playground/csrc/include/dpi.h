#ifndef __DPI_H__
#define __DPI_H__

#include "cpu.h"

#define npc_alert(expr, halt_code) do { if (expr) { } else { if (npc_state == CPU_RUN) {npc_state = halt_code;} return 0; } } while(0)

#endif
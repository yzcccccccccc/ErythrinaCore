#ifndef __DPI_H__
#define __DPI_H__

#include "cpu.h"

#define npc_alert(expr) do { if (expr) { } else { stop = CPU_ABORT_MEMLEAK; return 0; } } while(0)

#endif
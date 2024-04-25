#include <cstdint>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "cpu.h"
#include "sdb.h"
#include "util.h"

int main(int argc, char** argv) {
    init_env(argc, argv);
    //CPU_sim();
    sdb_main();
    return 0;
}
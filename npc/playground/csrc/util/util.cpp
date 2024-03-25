#include "util.h"
#include "common.h"
#include "memory.h"

#include <bits/types/FILE.h>
#include <cstdint>
#include <cstdio>
#include <assert.h>
#include <cstring>

static char *img_file = NULL;

void load_img(){
    if (img_file == NULL){
        printf("Use defaut image.\n");
        memcpy(guest2host(PC_RSTVEC), default_inst, sizeof(default_inst));
        return;
    }

    FILE *fp = fopen(img_file, "rb");
    assert(fp != NULL);

    fseek(fp, 0, SEEK_END);
    uint32_t size = ftell(fp);

    assert(size < MEMSIZE);
    printf("The image is %s, size = %d\n", img_file, size);

    fseek(fp, 0, SEEK_SET);
    int ret = fread(guest2host(PC_RSTVEC), size, 1, fp);
    assert(ret == 1);

    fclose(fp);
}

// init environment, mainly copy .bin to memory...
void init_env(int argc, char **argv){
    if (argc >= 2)
        img_file = argv[1];
    load_img();
}
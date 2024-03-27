#include "util.h"
#include "common.h"
#include "memory.h"
#include "sdb.h"
#include "difftest.h"

#include <cstdlib>
#include <getopt.h>
#include <bits/types/FILE.h>
#include <cstdint>
#include <cstdio>
#include <assert.h>
#include <cstring>

static char *img_file = NULL;
static char *diff_so_file = NULL;
static int difftest_port = 1234;

static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"diff"     , required_argument, NULL, 'd'},
    {"port"     , required_argument, NULL, 'p'},
    {"help"     , no_argument      , NULL, 'h'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhl:d:p:e:", table, NULL)) != -1) {
    switch (o) {
      case 'b': set_batch_mode(); break;
      case 'p': sscanf(optarg, "%d", &difftest_port); break;
      case 'd': diff_so_file = optarg; break;
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode (TBD)\n");
        printf("\t-l,--log=FILE           output log to FILE (TBD)\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

long load_img(){
    if (img_file == NULL){
        printf("Use defaut image.\n");
        memcpy(guest2host(PC_RSTVEC), default_inst, sizeof(default_inst));
        return 8;
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
    return size;
}

// init environment, mainly copy .bin to memory...
void init_env(int argc, char **argv){
    parse_args(argc, argv);

    long img_size = load_img();

    init_difftest(diff_so_file, img_size, difftest_port);
}
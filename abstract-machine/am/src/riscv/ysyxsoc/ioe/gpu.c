#include <am.h>
#include "../ysyxsoc.h"

void __am_gpu_init() {
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = 640, .height = 480,
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  uint32_t *fb = (uint32_t *)(uintptr_t)(VGA_ADDR);
  uint32_t *pi = (uint32_t *)(uintptr_t)(ctl->pixels);
  for (int y = 0; y < ctl->h; y++){
    for (int x = 0; x < ctl->w; x++){
      fb[(x + ctl->x) + (y + ctl->y) * 640] = pi[x + y * ctl->w];
    }
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
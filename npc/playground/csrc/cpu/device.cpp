#include "device.h"
#include <cassert>
#include <cstddef>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <memory.h>
#include <sys/time.h>

typedef void(*io_callback_t)(uint32_t offset, bool iswrite);

typedef struct MMIO{
    char name[10];
    paddr_t mmio_base;
    uintptr_t real_ptr;     // real memory in native
    io_callback_t callback;
    
}mmio_t;

int mmio_index;
mmio_t mmio_map[DEVICE_NUM];

// Device Funcs
uint32_t try_device_read(paddr_t paddr, bool *suc){
    *suc = false;
    for (int i = 0; i < mmio_index; i++){
        if (paddr >= mmio_map[i].mmio_base && paddr <= mmio_map[i].mmio_base + 8){
            uint32_t offset = paddr - mmio_map[i].mmio_base;
            mmio_map[i].callback(offset, 0);
            *suc = true;
            return *(uint32_t *)(mmio_map[i].real_ptr + offset);
        }
    }
    return 0;
}

uint32_t try_device_write(paddr_t paddr, uint32_t data, uint32_t mask, bool *suc){
    uint32_t real_mask = 0;
    for (int i = 0; i < 4; i++){
        if (mask & 1){
            real_mask |= 0xff << (i * 8);
        }
        mask >>= 1;
    }
    
    *suc = false;
    for (int i = 0; i < mmio_index; i++){
        if (paddr >= mmio_map[i].mmio_base && paddr <= mmio_map[i].mmio_base + 8){
            uint32_t offset = paddr - mmio_map[i].mmio_base;
            uint32_t real_data = *(uint32_t *)(mmio_map[i].real_ptr + offset) & (~real_mask) | data & real_mask;
            *(uint32_t *)(mmio_map[i].real_ptr + offset) = real_data;
            mmio_map[i].callback(offset, 1);
            *suc = true;
            return real_data;
        }
    }
    return 0;
}

// Serial
static uint8_t *serial_base = NULL;
void serial_handler(uint32_t offset, bool iswrite){
    assert(iswrite);
    printf("%c", serial_base[0]);
    //putchar(serial_base[0]);
}

void init_serial(){
    serial_base = (uint8_t *)malloc(8);
    strcpy(mmio_map[mmio_index].name, "serial");
    mmio_map[mmio_index].mmio_base  = SERIAL_MMIO;
    mmio_map[mmio_index].real_ptr   = (uintptr_t)serial_base;
    mmio_map[mmio_index].callback   = serial_handler;
    mmio_index++;
}

// Timer
static uint32_t *rtc_base = NULL;

uint64_t boot_time = 0;
uint64_t gettime(){
    struct timeval tv;

    gettimeofday(&tv, NULL);

    uint64_t t = tv.tv_sec * 1000000 + tv.tv_usec;

    if (boot_time == 0)
        boot_time = t;
    return t - boot_time;
}

void rtc_handler(uint32_t offset, bool iswrite){
    assert(!iswrite);
    if (offset == 0){
        uint64_t t = gettime();
        rtc_base[0] = (uint32_t)(t & 0xffffffff);
        rtc_base[1] = (uint32_t)(t >> 32);
    }
}

void init_timer(){
    gettime();
    rtc_base = (uint32_t *)malloc(8);
    strcpy(mmio_map[mmio_index].name, "rtc");
    mmio_map[mmio_index].mmio_base  = RTC_MMIO;
    mmio_map[mmio_index].real_ptr   = (uintptr_t)rtc_base;
    mmio_map[mmio_index].callback   = rtc_handler;
    mmio_index++;
}

void init_device(){
    init_serial();
    init_timer();
}
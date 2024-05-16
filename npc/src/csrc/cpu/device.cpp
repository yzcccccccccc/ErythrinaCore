#include "device.h"
#include "setting.h"
#include <cassert>
#include <cstdio>

uint32_t public_dev_addr;

uint8_t* try_device_write(paddr_t paddr, uint32_t *data, uint32_t mask){
    // only support UART Write
    if (paddr == DEV_UART){
        uint32_t real_mask = 0;
        for (int i = 0; i < 4; i++){
            if (mask & 1){
                real_mask |= 0xff << (i * 8);
            }
            mask >>= 1;
        }
        uint32_t real_data = *data & real_mask;
        printf("%c", (uint8_t)real_data);
        fflush(stdout);
        return (uint8_t*)(&public_dev_addr);
    }
    return 0;
}
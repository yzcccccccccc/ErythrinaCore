#ifndef __DEVICE_H__
#define __DEVICE_H__

#include <cstdint>
#include <memory.h>

#define DEVICE_NUM 100
#define SERIAL_MMIO 0xa00003f8
#define I8042_DATA_MMIO 0xa0000060
#define RTC_MMIO 0xa0000048

// device funcs
extern uint32_t try_device_write(paddr_t paddr, uint32_t data, uint32_t mask, bool *suc);
extern uint32_t try_device_read(paddr_t paddr, bool *suc);
extern void init_device();
// Serial
extern void init_serial();

// Timer
extern void init_timer();

#endif
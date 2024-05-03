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

#define DEV_CLINT       0x02000000
#define DEV_CLINT_SZ    0x10000

#define DEV_UART        0x10000000
#define DEV_UART_SZ     0x1000

#define DEV_SPI         0x10001000
#define DEV_SPI_SZ      0x2000

#define DEV_GPIO        0x10002000
#define DEV_GPIO_SZ     0x1000

#define DEV_PS2         0x10011000
#define DEV_PS2_SZ      0x8

#endif
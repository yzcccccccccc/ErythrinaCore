#ifndef __DEVICE_H__
#define __DEVICE_H__

#include <cstdint>
#include <memory.h>

extern uint32_t public_dev_addr;
// suc:1, fail:0
extern uint8_t* try_device_write(paddr_t paddr, uint32_t *data, uint32_t mask);

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

#define DEV_VGA         0x21000000
#define DEV_VGA_SZ      0x200000

#endif
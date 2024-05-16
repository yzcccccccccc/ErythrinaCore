#ifndef __NPC_H__
#define __NPC_H__

#include "../riscv.h"

# define DEVICE_BASE 0xa0000000


#define MMIO_BASE 0xa0000000

#define UART_BASE       0x10000000
#define SERIAL_PORT     (UART_BASE)


#define KBD_ADDR        0x10011000

#define RTC_ADDR        0x02000000

#define VGACTL_ADDR     (DEVICE_BASE + 0x0000100)
#define AUDIO_ADDR      (DEVICE_BASE + 0x0000200)
#define DISK_ADDR       (DEVICE_BASE + 0x0000300)
#define FB_ADDR         (MMIO_BASE   + 0x1000000)
#define AUDIO_SBUF_ADDR (MMIO_BASE   + 0x1200000)

#endif
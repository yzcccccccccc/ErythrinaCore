#ifndef __YSYXSOC_H__
#define __YSYXSOC_H__

#include "../riscv.h"

// UART
#define UART_BASE               0x10000000
#define UART_RBR                (UART_BASE)
#define UART_THR                (UART_BASE)
#define UART_DLB0               (UART_BASE)
#define UART_DLB1               (UART_BASE + 1)
#define UART_IER                (UART_BASE + 1)
#define UART_IIR                (UART_BASE + 2)
#define UART_FCR                (UART_BASE + 2)
#define UART_LCR                (UART_BASE + 3)
#define UART_MCR                (UART_BASE + 4)
#define UART_LSR                (UART_BASE + 5)
#define UART_MSR                (UART_BASE + 6)

// FLASH
#define FLASH_BASE              0x30000000

#define SET_BIT(addr, bit)      (*(volatile uint8_t  *)addr |= (1 << bit))
#define CLR_BIT(addr, bit)      (*(volatile uint8_t  *)addr &= (~(1 << bit)))

#define RTC_ADDR    0x02000000      // TODO clint
#define KBD_ADDR    0x10011000

#endif
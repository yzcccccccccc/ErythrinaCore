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

// SPI
#define SPI_BASE                0x10001000
#define SPI_RX0                 (SPI_BASE + 0x00)
#define SPI_RX1                 (SPI_BASE + 0x04)
#define SPI_RX2                 (SPI_BASE + 0x08)
#define SPI_RX3                 (SPI_BASE + 0x0c)
#define SPI_TX0                 (SPI_BASE + 0x00)
#define SPI_TX1                 (SPI_BASE + 0x04)
#define SPI_TX2                 (SPI_BASE + 0x08)
#define SPI_TX3                 (SPI_BASE + 0x0c)
#define SPI_CTR                 (SPI_BASE + 0x10)
#define SPI_DIV                 (SPI_BASE + 0x14)
#define SPI_SS                  (SPI_BASE + 0x18)

#define SPI_CTR_ASS_BIT         0x1000
#define SPI_CTR_IE_BIT          0x0800
#define SPI_CTR_LSB_BIT         0x0400
#define SPI_CTR_TxNEG_BIT       0x0200
#define SPI_CTR_RxNEG_BIT       0x0100
#define SPI_CTR_GOBSY_BIT       0x0080
#define SPI_CTR_CHARLEN_BIT     0x003f    


#define SET_BIT(addr, bit)      (*(volatile uint8_t  *)addr |= (1 << bit))
#define CLR_BIT(addr, bit)      (*(volatile uint8_t  *)addr &= (~(1 << bit)))
#define SET_MSK(addr, mask)     (*(volatile uint8_t  *)addr |= mask)           // set the masked bit
#define CLR_MSK(addr, mask)     (*(volatile uint8_t  *)addr &= mask)           // clr the masked bit

#define RTC_ADDR    0x02000000      // TODO clint
#define KBD_ADDR    0x10011000

#endif
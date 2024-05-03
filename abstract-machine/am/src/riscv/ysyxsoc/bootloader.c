#include "ysyxsoc.h"
// bootloader
extern void _trm_init();

void fsbl() __attribute__((section(".fsbl")));
void ssbl() __attribute__((section(".ssbl")));

void bios_uart_init() __attribute__((section(".fsbl")));
void bios_uart_putch(char ch) __attribute__((section(".fsbl")));
void bios_uart_puts(char *str) __attribute__((section(".fsbl")));

void bios_uart_init(){
    // Enable DLAB
    SET_BIT(UART_LCR, 7);

    // Set DLB  (Baud Rate: 600)
    outb(UART_DLB1, 0x00);
    outb(UART_DLB0, 0xc0);

    // Disable DLAB
    CLR_BIT(UART_LCR, 7);

    // Reset FIFO
    SET_BIT(UART_FCR, 1);
    SET_BIT(UART_FCR, 2);
}

void bios_uart_putch(char ch) {
  volatile uint8_t* uart_lsr = (volatile uint8_t *)UART_LSR;
  while (~(volatile uint8_t)(*uart_lsr) & (1 << 5));
  outb(UART_THR, ch);
}

void bios_uart_puts(char *str) {
    while (*str) {
        bios_uart_putch(*str++);
    }
}

// XIP in flash. load ssble to SRAM
extern char _ssbl_ld_start, _ssbl_ld_end;
extern char _ssbl_start, _ssbl_end;

void fsbl(){
    bios_uart_init();
    char *src = &_ssbl_ld_start;
    char *dst = &_ssbl_start;
    while (dst < &_ssbl_end){
        *dst++ = *src++;
    }
    
    bios_uart_putch('F');
    bios_uart_putch('D');
    bios_uart_putch('\n');

    ssbl();
}

// SRAM. load .text, .data, and .bss to PSRAM
extern char _ssbl_cp_start, _ssbl_cp_end;
extern char _ssbl_cp_dst_start, _ssbl_cp_dst_end;
extern char _ssbl_bss_clr_start, _ssbl_bss_clr_end;

void ssbl_memcpy(char *src, char *dst, char *dst_end) __attribute__((section(".ssbl")));
void ssbl_memclr(char *dst, char *dst_end) __attribute__((section(".ssbl")));

void ssbl_memcpy(char *src, char *dst, char *dst_end){
    uint32_t *src32 = (uint32_t *)src;
    uint32_t *dst32 = (uint32_t *)dst;
    uint32_t *dst_end32 = (uint32_t *)dst_end;
    while (dst32 < dst_end32){
        *dst32++ = *src32++;
    }
    if (dst32 == dst_end32)
        return;

    uint16_t *src16 = (uint16_t *)(src32 - 1);
    uint16_t *dst16 = (uint16_t *)(dst32 - 1);
    uint16_t *dst_end16 = (uint16_t *)dst_end;
    while (dst16 < dst_end16){
        *dst16++ = *src16++;
    }
    if (dst16 == dst_end16)
        return;

    uint8_t *src8 = (uint8_t *)(src16 - 1);
    uint8_t *dst8 = (uint8_t *)(dst16 - 1);
    uint8_t *dst_end8 = (uint8_t *)dst_end;
    while (dst8 < dst_end8){
        *dst8++ = *src8++;
    }
}

void ssbl_memclr(char *dst, char *dst_end){
    uint32_t *dst32 = (uint32_t *)dst;
    uint32_t *dst_end32 = (uint32_t *)dst_end;
    while (dst32 < dst_end32){
        *dst32++ = 0;
    }
    if (dst32 == dst_end32)
        return;

    uint16_t *dst16 = (uint16_t *)(dst32 - 1);
    uint16_t *dst_end16 = (uint16_t *)dst_end;
    while (dst16 < dst_end16){
        *dst16++ = 0;
    }
    if (dst16 == dst_end16)
        return;

    uint8_t *dst8 = (uint8_t *)(dst16 - 1);
    uint8_t *dst_end8 = (uint8_t *)dst_end;
    while (dst8 < dst_end8){
        *dst8++ = 0;
    }
}

void ssbl(){
    ssbl_memcpy(&_ssbl_cp_start, &_ssbl_cp_dst_start, &_ssbl_cp_dst_end);
    bios_uart_puts("[SSBL] copy .text, .data\n");

    ssbl_memclr(&_ssbl_bss_clr_start, &_ssbl_bss_clr_end);
    bios_uart_puts("[SSBL] clear .bss\n");

    bios_uart_puts("[SSBL] jump to _trm_init...\n");

    // jump to _trm_init
    asm volatile(
        "mv s0, zero;"
        "la sp, _stack_pointer;"
        "la t0, _trm_init;"
        "jalr ra, t0, 0;"
        :
        :
    );
}
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
    bios_uart_putch('\n');
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
extern char _text_ld_start, _text_ld_end, _text_start, _text_end;
extern char _data_ld_start, _data_ld_end, _data_start, _data_end;
extern char _rodata_ld_start, _rodata_ld_end, _rodata_start, _rodata_end;
extern char _bss_start, _bss_end;

extern char _has_data_extra;
extern char _has_bss_extra;

void ssbl(){
    // .rodata
    for (char *src = &_rodata_ld_start, *dst = &_rodata_start; dst < &_rodata_end;){
        *dst++ = *src++;
    }
    bios_uart_puts("[SSBL] load .rodata done");

    // .text
    for (char *src = &_text_ld_start, *dst = &_text_start; dst < &_text_end;){
        *dst++ = *src++;
    }
    bios_uart_puts("[SSBL] load .text done");

    // .data
    for (char *src = &_data_ld_start, *dst = &_data_start; dst < &_data_end;){
        *dst++ = *src++;
    }
    bios_uart_puts("[SSBL] load .data done");

    // .bss
    for (char *dst = &_bss_start; dst < &_bss_end;){
        *dst++ = 0;
    }
    bios_uart_puts("[SSBL] load .bss done");


    if (&_has_bss_extra == (char *)1){
        extern char _bss_extra_start, _bss_extra_end;

        // .bss_extra
        for (char *dst = &_bss_extra_start; dst < &_bss_extra_end;){
            *dst++ = 0;
        }
        bios_uart_puts("[SSBL] load .bss_extra done");
    }

    if (&_has_data_extra == (char *)1){
        extern char _data_extra_start, _data_extra_end, _data_extra_ld_start;

        // .data_extra
        for (char *src = &_data_extra_ld_start, *dst = &_data_extra_start; dst < &_data_extra_end;){
            *dst++ = *src++;
        }
        bios_uart_puts("[SSBL] load .data_extra done");
    }

    bios_uart_puts("[SSBL] jump to _trm_init...");

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
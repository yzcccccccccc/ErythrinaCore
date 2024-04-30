#include "ysyxsoc.h"
// bootloader
extern void _trm_init();

void fsbl() __attribute__((section(".fsbl"), optimize("O1")));
void ssbl() __attribute__((section(".ssbl"), optimize("O1")));

// XIP in flash. load ssble to SRAM
extern char _ssbl_ld_start, _ssbl_ld_end;
extern char _ssbl_start, _ssbl_end;

void fsbl(){
    char *src = &_ssbl_ld_start;
    char *dst = &_ssbl_start;
    while (dst < &_ssbl_end){
        *dst++ = *src++;
    }
    ssbl();
}

// SRAM. load .text, .data, and .bss to PSRAM
extern char _text_ld_start, _text_ld_end, _text_start, _text_end;
extern char _data_ld_start, _data_ld_end, _data_start, _data_end;
extern char _rodata_ld_start, _rodata_ld_end, _rodata_start, _rodata_end;
extern char _bss_start, _bss_end;
void ssbl(){
    // .text
    for (char *src = &_text_ld_start, *dst = &_text_start; dst < &_text_end;){
        *dst++ = *src++;
    }

    // .rodata
    for (char *src = &_rodata_ld_start, *dst = &_rodata_start; dst < &_rodata_end;){
        *dst++ = *src++;
    }

    // .data
    for (char *src = &_data_ld_start, *dst = &_data_start; dst < &_data_end;){
        *dst++ = *src++;
    }

    // .bss
    for (char *dst = &_bss_start; dst < &_bss_end;){
        *dst++ = 0;
    }

    // jump to _trm_init
    asm volatile(
        "mv s0, zero;"
        "la sp, _stack_pointer"
        :
        :
    );
    _trm_init();
}
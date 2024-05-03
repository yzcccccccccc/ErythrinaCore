#include <am.h>
#include "../ysyxsoc.h"

void __am_uart_init() {
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

void __am_uart_config(AM_UART_CONFIG_T *cfg) {
    cfg->present = true;
}

void __am_uart_tx(AM_UART_TX_T *uart) {
    putch(uart->data);
}

void __am_uart_rx(AM_UART_RX_T *uart) {
    uint8_t uart_lsr = inb(UART_LSR);
    if (uart_lsr & 1){
        uart->data  = inb(UART_RBR);
    }
    else{
        uart->data = 0xff;
    }
}
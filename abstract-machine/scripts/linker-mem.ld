MEMORY {
  mrom(rx)    : ORIGIN = 0x20000000, LENGTH = 4K
  sram(rwx)   : ORIGIN = 0x0f000000, LENGTH = 8K
  flash(rx)   : ORIGIN = 0x30000000, LENGTH = 8M
  psram(rwx)  : ORIGIN = 0x80000000, LENGTH = 4M
  pdram(rwx)  : ORIGIN = 0xa0000000, LENGTH = 16M
}
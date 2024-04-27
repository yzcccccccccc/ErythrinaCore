ENTRY(_start)
PHDRS { text PT_LOAD; data PT_LOAD; }

MEMORY {
  mrom(rx)  : ORIGIN = 0x20000000, LENGTH = 4K
  sram(rw)  : ORIGIN = 0x0f000000, LENGTH = 8K
  flash(rx) : ORIGIN = 0x30000000, LENGTH = 10M
}

SECTIONS {
  . = ORIGIN(flash);
  .text . : {
    *(entry)
    *(.text*)
  }>flash AT>flash : text
  etext = .;
  _etext = .;

  .rodata : {
    *(.rodata*)
  } >flash AT>flash

  _mrom_data_start = LOADADDR(.data);
  .data : {
    _data_start = .;
    *(.data*)
    *(.sdata*);
    _data_end = .;
  } >sram AT>flash : data
  edata = .;
  _data = .;

  .bss : {
	  _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon);
    _bss_end = .;
  } >sram AT>flash
  
  /* _stack top are defined in LDFLAGS */
  . = ORIGIN(sram);
  _heap_start = .;
  . = ORIGIN(sram) + 0x2000;
  _stack_pointer = .;
  _end = .;
}

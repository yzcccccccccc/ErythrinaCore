ENTRY(_start)
PHDRS { text PT_LOAD; data PT_LOAD; }

MEMORY {
  mrom(rx) : ORIGIN = 0x20000000, LENGTH = 4K
  sram(rw) : ORIGIN = 0x0f000000, LENGTH = 8K
}

SECTIONS {
  . = ORIGIN(mrom);
  .text . : {
    *(entry)
    *(.text*)
  }>mrom AT>mrom : text
  _etext = .;

  .rodata : {
    *(.rodata*)
  } >mrom AT>mrom

  _mrom_data_start = LOADADDR(.data);
  .data : {
    _data_start = .;
    *(.data*)
    *(.sdata*);
    _data_end = .;
  } >sram AT>mrom : data

  .bss : {
	  _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon);
    _bss_end = .;
  } >sram AT>mrom
  
  /* _stack top are defined in LDFLAGS */
  . = ORIGIN(sram);
  _heap_start = .;
  . = ORIGIN(sram) + 0x2000;
  _stack_pointer = .;
  _end = .;
}

ENTRY(_start)
PHDRS { text PT_LOAD; data PT_LOAD; }

SECTIONS {
  /* _mrom_start and _entry_offset are defined in LDFLAGS */
  . = _mrom_start + _entry_offset;
  .text : {
    *(entry)
    *(.text*)
  } : text
  etext = .;
  _etext = .;
  .rodata : {
    *(.rodata*)
  }
  .data : {
    *(.data)
  } : data
  edata = .;
  _data = .;
  .bss : {
	_bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  }
  
  /* _stack top are defined in LDFLAGS */
  . = _stack_top;
  _heap_start = .;
  . = _stack_top + 0x2000;
  _stack_pointer = .;
  end = .;
  _end = .;
}

ENTRY(_start)
PHDRS { text PT_LOAD; data PT_LOAD; }

MEMORY{
    mrom : ORIGIN = 0x20000000, LENGTH = 4K
    sram : ORIGIN = 0x0f000000, LENGTH = 8K
}
SECTIONS
  {
  .text 0x1000 : { 
    *(entry)
    *(.text*)
  } : text
  etext = .;
  _etext = . ; 
  _erodata = .;

  .mdata 0x2000 :
    AT (ORIGIN(mrom))
    { _data_start = . ; *(.data*); _data_end = . ;  }
  .bss 0x3000 : {
    _bss_start = . ;
    *(.bss*)
    *(.sbss*)
    *(.scommon);
    _bss_end = . ;
  }
  . = ORIGIN(sram);
  . = _stack_top;
  _heap_start = .;
  . = _stack_top + 0x2000;
  _stack_pointer = .;
}
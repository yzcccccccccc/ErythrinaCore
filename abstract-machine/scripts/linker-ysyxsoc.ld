INCLUDE "/home/yzcc/ysyx-workbench/abstract-machine/scripts/linker-mem.ld"

ENTRY(_start)

SECTIONS {
  _has_data_extra = 0;
  _has_bss_extra = 0;
  . = ORIGIN(flash);
  .fsbl :{
    *(entry)
    *(.fsbl*)
  }>flash AT>flash

  _ssbl_ld_start  = LOADADDR(.ssbl);
  .ssbl :{
    _ssbl_start = .;
    *(.ssbl*);
    _ssbl_end = .;
  }>sram AT>flash
  _ssbl_ld_end    = LOADADDR(.ssbl) + SIZEOF(.ssbl);

  _text_ld_start  = LOADADDR(.text);
  .text : {
    _text_start = .;
    *(.text*);
    _text_end = .;
  }>psram AT>flash
  _text_ld_end    = LOADADDR(.text) + SIZEOF(.text);

  _rodata_ld_start  = LOADADDR(.rodata);
  .rodata : {
    _rodata_start = .;
    *(.rodata*)
    *(.srodata*);
    _rodata_end = .;
  } >psram AT>flash
  _rodata_ld_end    = LOADADDR(.rodata) + SIZEOF(.rodata);

  _data_ld_start  = LOADADDR(.data);
  .data : {
    _data_start = .;
    *(.data*)
    *(.sdata*);
    _data_end = .;
  } >psram AT>flash
  _data_ld_end    = LOADADDR(.data) + SIZEOF(.data);

  _bss_ld_start  = LOADADDR(.bss);
  .bss (NOLOAD) : {
	  _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon);
    _bss_end = .;
  } >psram AT>flash
  _bss_ld_end    = LOADADDR(.bss) + SIZEOF(.bss);

  _heap_start = ORIGIN(psram) + 0x300000;
  
  _stack_pointer = ORIGIN(sram) + LENGTH(sram);
}

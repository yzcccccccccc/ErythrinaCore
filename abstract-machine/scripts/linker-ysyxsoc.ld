INCLUDE "/home/yzcc/ysyx-workbench/abstract-machine/scripts/linker-mem.ld"

ENTRY(_start)

SECTIONS {
  _has_data_extra = 0;
  _has_bss_extra = 0;
  _bss_extra_start = 0;
  _bss_extra_end = 0;
  _data_extra_start = 0;
  _data_extra_end = 0;
  _data_extra_ld_start = 0;

  . = ORIGIN(flash);
  .fsbl :{
    *(entry)
    *(.fsbl*)
  }>flash AT>flash

  /* fsbl will copy this*/
  _ssbl_ld_start  = LOADADDR(.ssbl);
  .ssbl : ALIGN(8){
    . = ALIGN(8);
    _ssbl_start = .;
    *(.ssbl*);
    _ssbl_end = .;
    . = ALIGN(8);
  }>sram AT>flash
  _ssbl_ld_end    = LOADADDR(.ssbl) + SIZEOF(.ssbl);

  /* ssbl will copy this*/
  _ssbl_cp_start  = LOADADDR(.text);
  .text : ALIGN(8) {
    . = ALIGN(8);
    _ssbl_cp_dst_start = .;

    *(.text*);
    . = ALIGN(8);
  }>pdram AT>flash

  .rodata : ALIGN(8){
    . = ALIGN(8);
    *(.rodata*)
    *(.srodata*);
    . = ALIGN(8);
  } >pdram AT>flash

  .data : ALIGN(8){
    . = ALIGN(8);
    *(.data*)
    *(.sdata*);
    . = ALIGN(8);

    _ssbl_cp_dst_end = .;
  } >pdram AT>flash
  _ssbl_cp_end = LOADADDR(.data) + SIZEOF(.data);

  /* ssbl will clear this*/
  .bss : ALIGN(8){
    . = ALIGN(8);
	  _ssbl_bss_clr_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon);
    . = ALIGN(8);
    _ssbl_bss_clr_end = .;
  } >pdram AT>flash

  _heap_start = ORIGIN(pdram) + 0x300000;
  
  _stack_pointer = ORIGIN(sram) + LENGTH(sram);
}

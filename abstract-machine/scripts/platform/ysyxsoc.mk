AM_SRCS	:=	riscv/ysyxsoc/start.S \
			riscv/ysyxsoc/trm.c \
			riscv/ysyxsoc/ioe.c \
			riscv/ysyxsoc/timer.c \
			riscv/ysyxsoc/input.c \
			riscv/ysyxsoc/cte.c \
			riscv/ysyxsoc/trap.S \
			platform/dummy/vme.c \
			platform/dummy/mpe.c

CFLAGS	+= -fdata-sections -ffunction-sections
LDFLAGS	+= -T $(AM_HOME)/scripts/ysyxsoc-linker.ld \
			--defsym=_mrom_start=0x20000000 --defsym=_entry_offset=0x0 --defsym=_stack_top=0x0f000000
LDFLAGS += --gc-sections -e _start
CFLAGS	+= -DMAINARGS=\"$(mainargs)"\
.PHONY: $(AM_HOME)/am/src/riscv/ysyx/trm.c

NPC_ARGS ?= 

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin

run: image
	$(MAKE) -C $(NPC_HOME) sim ARG=$(NPC_ARGS) IMG=$(IMAGE).bin

view_wave:
	$(MAKE) -C $(NPC_HOME) view_wave

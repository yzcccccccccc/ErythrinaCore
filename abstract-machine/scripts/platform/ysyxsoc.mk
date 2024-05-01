AM_SRCS	:=	riscv/ysyxsoc/start.S \
			riscv/ysyxsoc/trm.c \
			riscv/ysyxsoc/ioe.c \
			riscv/ysyxsoc/timer.c \
			riscv/ysyxsoc/input.c \
			riscv/ysyxsoc/cte.c \
			riscv/ysyxsoc/trap.S \
			riscv/ysyxsoc/bootloader.c\
			platform/dummy/vme.c \
			platform/dummy/mpe.c

CFLAGS	+= -fdata-sections -ffunction-sections -Os
LDFLAGS	+= -T $(AM_HOME)/scripts/linker-ysyxsoc.ld
LDFLAGS += --gc-sections -e _start -Map
CFLAGS	+= -DMAINARGS=\"$(mainargs)\"
.PHONY: $(AM_HOME)/am/src/riscv/ysyx/trm.c

NPC_ARGS ?= 

image: $(IMAGE).elf
	@$(OBJDUMP) -thD $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin

run: image
	$(MAKE) -C $(NPC_HOME) sim ARG=$(NPC_ARGS) IMG=$(IMAGE).bin

view_wave:
	$(MAKE) -C $(NPC_HOME) view_wave

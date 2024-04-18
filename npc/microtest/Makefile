OBJNAME 	?= 
BUILD_DIR 	?= build

LINKER		?= linker.ld

GCC		= riscv64-linux-gnu-gcc
OBJDUMP	= riscv64-linux-gnu-objdump
OBJCOPY	= riscv64-linux-gnu-objcopy
CFLAGS 	+= -march=rv64g -O2 -nostartfiles -o
LDFLAGS += -T $(LINKER)

ELF		= $(BUILD_DIR)/$(OBJNAME)
TXT		= $(BUILD_DIR)/$(OBJNAME).txt
BIN		= $(BUILD_DIR)/$(OBJNAME).bin

$(ELF): $(LINKER) $(OBJNAME).c
	-@mkdir -p $(BUILD_DIR)
	-@$(GCC) $(CFLAGS) $(BUILD_DIR)/$(OBJNAME) $(OBJNAME).c $(LDFLAGS)

$(TXT): $(ELF)
	-@$(OBJDUMP) $(BUILD_DIR)/$(OBJNAME) -d > $(BUILD_DIR)/$(OBJNAME).txt

$(BIN): $(ELF)
	-@$(OBJCOPY) --only-section=.text -O binary $(BUILD_DIR)/$(OBJNAME) $(BUILD_DIR)/$(OBJNAME).bin

gen: $(ELF) $(TXT) $(BIN)


all: gen
	
TOPNAME = TOP
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
$(shell mkdir -p $(BUILD_DIR))

export PATH := $(PATH):$(abspath ./utils)

# Tools
GTKWAVE = gtkwave
VERILATOR = verilator

# Flags
VERILATOR_NVFLAGS += -MMD --build -cc  \
				-O3 --x-assign fast --x-initial fast --noassert
VERILATOR_SIMFLAGS += --cc --exe --trace --build -j 0 -CFLAGS "-DVERILATOR_SIM"
VERILATOR_VERFLAGS += --cc

test:
	mill -i __.test

verilog:
	$(call git_commit, "generate verilog")
#	mkdir -p $(BUILD_DIR)
	mill -i __.test.runMain Elaborate --target-dir $(BUILD_DIR)

help:
	mill -i __.test.runMain Elaborate --help

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

# Verilator
CSRCS = $(shell find $(abspath ./playground/csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
VSRCS = $(shell find $(BUILD_DIR) -name "*.v")
SIM_TARGET = V$(TOPNAME)

verilate: verilog
	$(VERILATOR) $(VERILATOR_VERFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS)

verilate_sim: verilog
	$(VERILATOR) $(VERILATOR_SIMFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS) $(CSRCS)

sim: verilate_sim
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	$(OBJ_DIR)/$(SIM_TARGET)

view_wave:
	$(GTKWAVE) ./wave.vcd

# NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk
INC_PATH ?=
BIN = $(BUILD_DIR)/$(TOPNAME)
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
NXDC_FILES = playground/constr/top.nxdc

$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

CSRCS += $(SRC_AUTO_BIND)
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\"" -DNVBOARD

$(BIN): $(VSRCS) $(CSRCS) $(NVBOARD_ARCHIVE)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_NVFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

# Hint: Run 'make verilog' first!
run_nvb: $(BIN)
	@$^

clean:
	-rm -rf $(BUILD_DIR)
	-rm -rf wave.vcd

.PHONY: test verilog help compile bsp reformat checkformat clean

-include ../Makefile

TOPNAME = TOP
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
$(shell mkdir -p $(BUILD_DIR))

export PATH := $(PATH):$(abspath ./utils)

# Tools
GTKWAVE = gtkwave
VERILATOR = verilator

# Flags
ERILATOR_NVFLAGS += -MMD --build -cc  \
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

clean:
	-rm -rf $(BUILD_DIR)
	-rm -rf wave.vcd

.PHONY: test verilog help compile bsp reformat checkformat clean

-include ../Makefile

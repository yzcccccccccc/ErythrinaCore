TOPNAME ?= ErythrinaCore
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
VERILATOR_SIMFLAGS += -cc --exe --trace --build -j -CFLAGS "-DVERILATOR_SIM"
VERILATOR_VERFLAGS += -cc --trace

# Mill
MILL_ARGS_ALL += --split-verilog

test:
	mill -i __.test

verilog:
	$(call git_commit, "generate verilog")
#	mkdir -p $(BUILD_DIR)
	mill -i __.test.runMain Elaborate --target-dir $(BUILD_DIR) $(MILL_ARGS_ALL)

help:
	mill -i __.test.runMain Elaborate --help $(MILL_ARGS_ALL)

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

# Verilator
# VCSRCS = $(shell find $(abspath ./playground/csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
VCSRCS ?= $(shell find $(abspath ./playground/csrc) -name "main.cpp")		# for verilator
CSRCS ?= $(shell find $(abspath ./playground/csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")		# for NVBOARD
VSRCS ?= $(shell find $(abspath $(BUILD_DIR)) -name "*.v" -or -name "*.sv")
SIM_TARGET = V$(TOPNAME)

verilate:
	$(VERILATOR) $(VERILATOR_VERFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS)

verilate_sim:
	$(VERILATOR) $(VERILATOR_SIMFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS) $(VCSRCS)

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

# yosys tools
YOSYSTA_PATH = ../yosys-sta
SDC_FILE = $(PWD)/playground/constr/top.sdc
RESDIR = $(abspath $(BUILD_DIR))

run_syn:
	$(MAKE) -C $(YOSYSTA_PATH) sta RES_PATH=$(RESDIR) DESIGN=$(TOPNAME) SDC_FILE=$(SDC_FILE) RTL_FILES="$(VSRCS)" CLK_FREQ_MHZ=100

clean:
	-rm -rf $(BUILD_DIR)
	-rm -rf wave.vcd

.PHONY: test verilog help compile bsp reformat checkformat clean

-include ../Makefile

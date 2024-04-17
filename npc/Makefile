TOPNAME ?= ysyxSoCFull
YSYXNAME ?= ysyx_1919810

BUILD_DIR = ./build
YSYXSOC_DIR = ../ysyxSoC
OBJ_DIR = $(BUILD_DIR)/obj_dir

$(shell mkdir -p $(BUILD_DIR))
export PATH := $(PATH):$(abspath ./utils)

# Tools
GTKWAVE = gtkwave
VERILATOR = verilator

# for llvm
CXXFLAGS += $(shell llvm-config --cxxflags) -fPIE
LIBS += $(shell llvm-config --libs)

# Mill
MILL_ARGS_ALL += --split-verilog --throw-on-first-error

test:
	mill -i __.test

verilog:
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate --target-dir $(BUILD_DIR) $(MILL_ARGS_ALL)
	-@ sed -i -e 's/_\(master\|slave\)_\(aw\|ar\|w\|r\|b\)_\(\|bits_\)/_\1_\2/g' $(BUILD_DIR)/$(YSYXNAME).sv

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
CSRCS 		?= $(shell find $(abspath ./playground/csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")		# for NVBOARD
VSRCS 		?= $(shell find $(abspath $(BUILD_DIR)) -name "*.v" -or -name "*.sv")
SOC_PERIP 	?= $(shell find $(abspath $(YSYXSOC_DIR)/perip) -name "*.v")
SOC_TOP		?= $(YSYXSOC_DIR)/generated/ysyxSoCFull.v
UART_RTLDIR ?= $(YSYXSOC_DIR)/perip/uart16550/rtl
SPI_RTLDIR 	?= $(YSYXSOC_DIR)/perip/spi/rtl
SIM_TARGET = V$(TOPNAME)

IMG ?= 
ARG ?= 
DIFF_SO ?= $(NEMU_HOME)/build/riscv32-nemu-interpreter-so

# Flags
VERILATOR_NVFLAGS += -MMD --build -cc  \
				-O3 --x-assign fast --x-initial fast --noassert
VLDFLAGS += -lreadline ${LIBS}
VCFLAGS += -DVERILATOR_SIM -I /home/yzcc/ysyx-workbench/npc/playground/csrc/include ${CXXFLAGS}\

SOC_FLAGS += -I$(UART_RTLDIR) -I$(SPI_RTLDIR) --timescale "1ns/1ns" --no-timing
VERILATOR_SIMFLAGS += -cc --exe --trace --build -j -CFLAGS "${VCFLAGS}" -LDFLAGS "${VLDFLAGS}"
VERILATOR_VERFLAGS += -cc --trace  

VSRCS += $(SOC_PERIP)
VSRCS += $(SOC_TOP)
VERILATOR_SIMFLAGS	+= $(SOC_FLAGS)
VERILATOR_VERFLAGS	+= $(SOC_FLAGS)
verilate: verilog
	$(VERILATOR) $(VERILATOR_VERFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS)

verilate_sim:
	$(VERILATOR) $(VERILATOR_SIMFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS) $(CSRCS)

sim: verilate_sim
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	$(OBJ_DIR)/$(SIM_TARGET) -d $(DIFF_SO) -b $(ARG) $(IMG)

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

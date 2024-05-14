BUILD_DIR	= ./build
YSYXSOC_DIR = ../ysyxSoC
OBJ_DIR		= $(BUILD_DIR)/obj_dir
RES_DIR		= $(BUILD_DIR)/report

$(shell mkdir -p $(BUILD_DIR))
$(shell mkdir -p $(OBJ_DIR))
$(shell mkdir -p $(RES_DIR))
export PATH := $(PATH):$(abspath ./utils)

# Tools
GTKWAVE		= gtkwave
VERILATOR	= verilator

IMG ?= 
ARG ?= 
DIFF_SO ?= $(NEMU_HOME)/build/riscv32-nemu-interpreter-so

-include ./script/compile.mk
-include ./script/verilator.mk
-include ./script/nvboard.mk
-include ./script/yosys.mk

view_wave:
	$(GTKWAVE) ./wave.vcd

# Micro Test
OBJNAME ?= char-test
micro-test: verilate_sim
	$(MAKE) -C microtest/ OBJNAME=$(OBJNAME) gen
	$(SIM_TAR) -d $(DIFF_SO) -b $(ARG) microtest/build/$(OBJNAME).bin

# Perf
PERF_BENCH ?= dummy
perf:
	@echo "Running Perf Test, bench: $(PERF_BENCH)"
	bash ./script/perf.sh $(PERF_BENCH)

clean:
	-rm -rf $(BUILD_DIR)
	-rm -rf wave.vcd

.PHONY: test verilog help compile bsp reformat checkformat clean

-include ../Makefile

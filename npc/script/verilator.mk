# For Verilator Compilation
OBJ_DIR		= $(BUILD_DIR)/obj_dir

SOC_PERIP 	?= $(shell find $(abspath $(YSYXSOC_DIR)/perip) -name "*.v")
SOC_TOP		?= $(YSYXSOC_DIR)/build/ysyxSoCFull.v
UART_RTLDIR ?= $(YSYXSOC_DIR)/perip/uart16550/rtl
SPI_RTLDIR 	?= $(YSYXSOC_DIR)/perip/spi/rtl

# for llvm
CXXFLAGS += $(shell llvm-config --cxxflags) -fPIE
LIBS += $(shell llvm-config --libs)

Verilator_LDFLG	+= -lreadline ${LIBS}
Verilator_CFLG	+= -DVERILATOR_SIM -I /home/yzcc/ysyx-workbench/npc/src/csrc/include ${CXXFLAGS}\

SOC_FLAGS 		+= -I$(UART_RTLDIR) -I$(SPI_RTLDIR) --timescale "1ns/1ns" --no-timing
Verilator_SFLG	+= -cc --exe --trace --build -j -CFLAGS "${Verilator_CFLG}" -LDFLAGS "${Verilator_LDFLG}" --autoflush
Verilator_VFLG	+= -cc --trace --build	# Only pack up the .v files

# SRC
VSRCS 		?= $(shell find $(abspath $(BUILD_DIR)/rtl) -name "*.v" -or -name "*.sv")
CSRCS 		?= $(shell find $(abspath ./src/csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
HSRCS		?= $(shell find $(abspath ./src/csrc) -name "*.h")

VSRCS += $(SOC_PERIP)
VSRCS += $(SOC_TOP)
Verilator_SFLG	+= $(SOC_FLAGS)
Verilator_VFLG	+= $(SOC_FLAGS)

Verilator_TAR	= $(OBJ_DIR)/V$(TOPNAME).h
$(VSRCS): verilog
$(Verilator_TAR): $(VSRCS)
	-@ $(VERILATOR) $(Verilator_VFLG) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS)

SIM_TAR			= $(OBJ_DIR)/V$(TOPNAME)
$(SIM_TAR): $(VSRCS) $(CSRCS) $(HSRCS)
	-@ $(VERILATOR) $(Verilator_SFLG) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(VSRCS) $(CSRCS)

verilate: $(Verilator_TAR)

verilate_sim: $(SIM_TAR)

sim: verilate_sim
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	$(SIM_TAR) -d $(DIFF_SO) -b $(ARG) $(IMG)

.PHONY: verilate verilate_sim
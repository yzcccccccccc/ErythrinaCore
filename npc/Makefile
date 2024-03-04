TOPNAME = top
NXDC_FILES = constr/top.nxdc
INC_PATH ?=

GTKWAVE = gtkwave
VERILATOR = verilator
VERILATOR_NVFLAGS += -MMD --build -cc  \
				-O3 --x-assign fast --x-initial fast --noassert
VERILATOR_SIMFLAGS += --cc --exe --trace --build -j 0 -Wall -CFLAGS "-DVERILATOR_SIM"
VERILATOR_VERFLAGS += --cc

BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)
  
default: $(BIN)

$(shell mkdir -p $(BUILD_DIR))

# NVBOARD constraint file
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

# NVBOARD project sources
VSRCS = $(shell find $(abspath ./vsrc) -name "*.v")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
CSRCS += $(SRC_AUTO_BIND)

# NVBOARD rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk

# NVBOARD rules for verilator
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\"" -DNVBOARD

$(BIN): $(VSRCS) $(CSRCS) $(NVBOARD_ARCHIVE)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_NVFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

# VERILATOR source
HDLSRC = $(shell find $(abspath ./vsrc) -name "*.v")
SFTSRC = $(shell find $(abspath ./csrc) -name "main.cpp")
SIM_TARGET = V$(TOPNAME)

all:

verilate:
	$(VERILATOR) $(VERILATOR_VERFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(HDLSRC)

verilate_sim:
	$(VERILATOR) $(VERILATOR_SIMFLAGS) --Mdir $(OBJ_DIR) --top-module $(TOPNAME) $(HDLSRC) $(SFTSRC)

sim: verilate_sim
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	$(OBJ_DIR)/$(SIM_TARGET)

view_wave:
	$(GTKWAVE) ./wave.vcd

run_nvb: $(BIN)
	@$^

clean:
	-rm -rf $(BUILD_DIR)
	-rm -rf wave.vcd

include ../Makefile

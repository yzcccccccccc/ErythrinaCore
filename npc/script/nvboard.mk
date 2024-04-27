# For NVBoard Compilation

# TBD

# NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk
INC_PATH ?=
BIN = $(BUILD_DIR)/$(TOPNAME)
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
NXDC_FILES = playground/constr/top.nxdc

$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

NVCSRCS ?= $(CSRCS)
NVCSRCS += $(SRC_AUTO_BIND)
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\"" -DNVBOARD

$(BIN): $(VSRCS) $(NVCSRCS) $(NVBOARD_ARCHIVE)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_NVFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

# Hint: Run 'make verilog' first!
run_nvb: $(BIN)
	@$^

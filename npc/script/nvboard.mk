# For NVBoard Compilation

# NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk
INC_PATH ?=
BIN = $(OBJ_DIR)/$(TOPNAME)

VERILATOR_NVFLAGS += --build -cc  --trace-fst\
				-O3 --x-assign fast --x-initial fast --noassert\
				-j -CFLAGS "$(Verilator_CFLG)" -LDFLAGS "$(Verilator_LDFLG)" --autoflush
VERILATOR_NVFLAGS += $(SOC_FLAGS)


SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
NXDC_FILES = src/constr/top.nxdc

$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

NVCSRCS ?= $(CSRCS)
NVCSRCS += $(SRC_AUTO_BIND)
INCFLAGS = $(addprefix -I, $(INC_PATH))
NVCXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\"" -DNVBOARD

$(BIN): $(VSRCS) $(NVCSRCS) $(NVBOARD_ARCHIVE) $(HSRCS)
	-@rm -rf $(OBJ_DIR)
	-@$(VERILATOR) $(VERILATOR_NVFLAGS) \
		--top-module $(TOPNAME) $(VSRCS) $(NVCSRCS) $(NVBOARD_ARCHIVE) \
		$(addprefix -CFLAGS , $(NVCXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

# Hint: Run 'make verilog' first!
run_nvb: $(BIN)
	$(BIN) -d $(DIFF_SO) -b $(ARG) $(IMG)

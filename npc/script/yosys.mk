# yosys tools
YOSYSTA_PATH = ../yosys-sta

RTL_STA_DIR = $(BUILD_DIR)/sta-rtl
$(shell mkdir -p $(RTL_STA_DIR))

V_FILE_TIME = $(RTL_STA_DIR)/TimingTop.sv
$(V_FILE_TIME): $(SCALA_FILES)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate_Time --target-dir $(RTL_STA_DIR) $(MILL_ARGS_ALL)

RES_PATH = $(abspath $(BUILD_DIR))/report

$(RTL_FILE): $(V_FILE_TIME)

SDC_FILE = $(PWD)/src/constr/top.sdc
RTL_FILE ?= $(shell find $(abspath $(RTL_STA_DIR)) \( -name "*.v" -or -name "*.sv" \) ! -name "halter_*" )
RESULT_BASE = $(abspath $(BUILD_DIR))

sta: $(V_FILE_TIME)
	@echo $(RTL_FILE)
	@$(MAKE) -C $(YOSYSTA_PATH) sta RES_PATH=$(RES_PATH) DESIGN=TimingTop SDC_FILE=$(SDC_FILE) RTL_FILES="$(RTL_FILE)" CLK_FREQ_MHZ=300 RESULT_BASE_DIR=$(RESULT_BASE)


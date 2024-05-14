# yosys tools
YOSYSTA_PATH = ../yosys-sta
RES_PATH = $(abspath $(RES_DIR))/sta-report

SDC_FILE = $(PWD)/src/constr/top.sdc
RTL_FILE = $(abspath $(VSTA_FILE))
RESULT_BASE = $(abspath $(RES_DIR))

sta: $(VSTA_FILE)
	@$(MAKE) -C $(YOSYSTA_PATH) sta RES_PATH=$(RES_PATH) DESIGN=TimingTop SDC_FILE=$(SDC_FILE) RTL_FILES="$(RTL_FILE)" CLK_FREQ_MHZ=300 RESULT_BASE_DIR=$(RESULT_BASE)


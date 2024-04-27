# yosys tools
YOSYSTA_PATH = ../yosys-sta
SDC_FILE = $(PWD)/playground/constr/top.sdc
RESDIR = $(abspath $(BUILD_DIR))

run_syn:
	$(MAKE) -C $(YOSYSTA_PATH) sta RES_PATH=$(RESDIR) DESIGN=$(TOPNAME) SDC_FILE=$(SDC_FILE) RTL_FILES="$(VSRCS)" CLK_FREQ_MHZ=100
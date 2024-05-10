YSYXNAME	?= ysyx_1919810

# For Erythcore Compilation
RTL_SOC_DIR	= $(BUILD_DIR)/rtl-soc
RTL_SIM_DIR	= $(BUILD_DIR)/rtl-sim
RTL_STA_DIR	= $(BUILD_DIR)/rtl-sta
$(shell mkdir -p $(RTL_SOC_DIR))
$(shell mkdir -p $(RTL_SIM_DIR))
$(shell mkdir -p $(RTL_STA_DIR))

SCALA_FILES = $(shell find src/vsrc/ -name "*.scala")
VSOC_FILE	= $(RTL_SOC_DIR)/$(YSYXNAME).sv
VSIM_FILE	= $(RTL_SIM_DIR)/SimTop.sv
VSTA_FILE	= $(RTL_STA_DIR)/TimingTop.sv

# Mill
MILL_ARGS_ALL += --throw-on-first-error

test:
	mill -i __.test

# for ysyxSoC
$(VSOC_FILE):$(SCALA_FILES)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate_Soc --target-dir $(RTL_SOC_DIR) $(MILL_ARGS_ALL)
	sed -i '/firrtl_black_box_resource_files.f/, $$d' $@
	sed -i -e 's/_\(master\|slave\)_\(aw\|ar\|w\|r\|b\)_\(\|bits_\)/_\1_\2/g' $@

# for Simulations
$(VSIM_FILE):$(SCALA_FILES)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate_Sim --target-dir $(RTL_SIM_DIR) $(MILL_ARGS_ALL)
	sed -i '/firrtl_black_box_resource_files.f/, $$d' $@

# for sta
$(VSTA_FILE):$(SCALA_FILES)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate_Time --target-dir $(RTL_STA_DIR) $(MILL_ARGS_ALL)
	sed -i '/firrtl_black_box_resource_files.f/, $$d' $@

verilog: $(VSOC_FILE) $(VSIM_FILE) $(VSTA_FILE)

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

.PHONY: verilog
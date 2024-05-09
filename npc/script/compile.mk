# For Erythcore Compilation
RTL_DIR		= $(BUILD_DIR)/rtl
$(shell mkdir -p $(RTL_DIR))

SCALA_FILES = $(shell find src/vsrc/ -name "*.scala")
V_FILE_TAR	= $(RTL_DIR)/$(YSYXNAME).sv

# Mill
MILL_ARGS_ALL += --throw-on-first-error --split-verilog

test:
	mill -i __.test

$(V_FILE_TAR):	$(SCALA_FILES)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate --target-dir $(RTL_DIR) $(MILL_ARGS_ALL)
	-@ sed -i -e 's/_\(master\|slave\)_\(aw\|ar\|w\|r\|b\)_\(\|bits_\)/_\1_\2/g' $(RTL_DIR)/$(YSYXNAME).sv

verilog: $(V_FILE_TAR)

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

.PHONY: verilog
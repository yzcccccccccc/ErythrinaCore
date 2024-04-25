# For Erythcore Compilation

SCALA_FILES = $(shell find playground)

# Mill
MILL_ARGS_ALL += --split-verilog --throw-on-first-error

test:
	mill -i __.test

verilog:
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate --target-dir $(BUILD_DIR)/rtl $(MILL_ARGS_ALL)
	-@ sed -i -e 's/_\(master\|slave\)_\(aw\|ar\|w\|r\|b\)_\(\|bits_\)/_\1_\2/g' $(BUILD_DIR)/$(YSYXNAME).sv

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
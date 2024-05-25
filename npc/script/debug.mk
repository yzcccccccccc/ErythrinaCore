# UT level debug? 

DEBUG_DIR 	= ./debug
UT_NAME		= Multiplier
UT_FILE		= $(DEBUG_DIR)/$(UT_NAME).sv
UT_DIR		= $(DEBUG_DIR)/picker_out_$(UT_NAME)

debug_verilog: $(SCALA_FILES)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate_Debug --target-dir $(DEBUG_DIR) $(MILL_ARGS_ALL)

$(UT_FILE): debug_verilog

gen_ut: $(UT_FILE)
	picker $(UT_FILE) -l python -t $(UT_DIR) -w $(UT_NAME).fst
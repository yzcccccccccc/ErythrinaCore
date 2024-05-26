# UT level debug? 

DEBUG_DIR 	= ./debug
UT_NAME		= MulDebug
UT_FILE		= $(DEBUG_DIR)/$(UT_NAME).sv
UT_DIR		= $(DEBUG_DIR)/picker_out_$(UT_NAME)

$(UT_FILE): $(SCALA_SRCS)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate_Debug --target-dir $(DEBUG_DIR) $(MILL_ARGS_ALL)

debug_verilog: $(UT_FILE)
	
gen_ut: $(UT_FILE)
	picker $(UT_FILE) -l python -t $(UT_DIR) -w $(UT_NAME).fst -c

.PHONY: debug_verilog
# UT level debug? 

DEBUG_DIR 	= ./debug
UT_NAME		= DivDebug
UT_FILE		= $(DEBUG_DIR)/$(UT_NAME).sv
UT_DIR		= $(DEBUG_DIR)/picker_out_$(UT_NAME)

$(UT_FILE): $(SCALA_FILES)
	$(call git_commit, "generate verilog")
	mill -i __.test.runMain Elaborate_Debug --target-dir $(DEBUG_DIR) $(MILL_ARGS_ALL)

debug_verilog: $(UT_FILE)
	
gen_ut: $(UT_FILE)
	@rm -rf $(UT_DIR)/$(UT_NAME)*.v
	@rm -rf $(UT_DIR)/$(UT_NAME)*.sv
	picker $(UT_FILE) -l python -t $(UT_DIR) -w $(UT_NAME).fst -c
	@echo -e "\n\n-include ./script/runner.mk" >> $(UT_DIR)/Makefile

.PHONY: debug_verilog
MODULE_NAME = top

HDLDIR = ./vsrc
SFTDIR = ./csrc

HDLSRC = $(HDLDIR)/$(MODULE_NAME).v
SFTSRC = $(SFTDIR)/main.cpp

SIM_TARGET = V$(MODULE_NAME)

all:

verilate:
	verilator --cc $(HDLSRC)

verilate_sim:
	verilator --cc --exe --trace --build -j 0 -Wall $(HDLSRC) $(SFTSRC)

sim: verilate_sim
#$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@cd obj_dir && ./$(SIM_TARGET)

clean:
	rm -rf obj_dir/* && rmdir obj_dir

include ../Makefile

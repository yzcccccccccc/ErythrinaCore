V_TARGET=top.v
SIM_TARGET=sim_top.cpp

all: 
	verilator --cc --exe --trace --build -j 0 -Wall $(SIM_TARGET) $(V_TARGET)
clean:
	rm -rf obj_dir/* && rmdir obj_dir
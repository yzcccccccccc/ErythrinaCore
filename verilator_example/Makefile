V_TARGET=our.v
SIM_TARGET=sim_main.cpp

all: 
	verilator --cc --exe --trace --build -j 0 -Wall $(SIM_TARGET) $(V_TARGET)
clean:
	rm -rf obj_dir/* && rmdir obj_dir
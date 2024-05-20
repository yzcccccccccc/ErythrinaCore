#!/bin/bash

# This script is used to run the performance test for the project.

echo "Running Static Timing Analysis..."
make sta
echo "Finish Static Timing Analysis. Performance result in ./build/report/result"

benchname=$1

# microbench
if [ $benchname == "microbench" ]; then
    echo "Run microbench"
    make -C ../am-kernels/benchmarks/microbench ARCH=riscv32e-ysyxsoc run mainargs=train
    echo "Finish microbench. Performance result in ./build/report/perf.log"
fi

# dhyrstone
if [ $benchname == "dhrystone" ]; then
    echo "Run dhrystone"
    make -C ../am-kernels/benchmarks/dhyrstone ARCH=riscv32e-ysyxsoc run
    echo "Finish dhrystone. Performance result in ./build/report/perf.log"
fi

# coremark
if [ $benchname == "coremark" ]; then
    echo "Run coremark"
    make -C ../am-kernels/benchmarks/coremark ARCH=riscv32e-ysyxsoc run
    echo "Finish coremark. Performance result in ./build/report/perf.log"
fi

# dummy
if [ $benchname == "dummy" ]; then
    echo "Run dummy"
    make -C ../am-kernels/tests/cpu-tests ARCH=riscv32e-ysyxsoc ALL=dummy run
    echo "Finish dummy. Performance result in ./build/report/perf.log"
fi

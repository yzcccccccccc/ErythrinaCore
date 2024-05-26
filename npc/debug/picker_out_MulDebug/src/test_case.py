from Multiplier import *

import mlvp.reporter as rp
import mlvp

import random
import numpy as np

async def mul_test(dut):
    dut:Multiplier = dut

    mlvp.create_task(mlvp.start_clock(dut))
    print('\n')
    for i in range(30):
        op  = 0
        a_s = 1 if op != 3 else 0               # signed or unsigned
        b_s = 1 if (op >> 1) == 0 else 0        # signed or unsigned

        a_src = None
        if (a_s == 0):
            a_src = np.uint32(random.randint(0, 32))
        else:
            a_src = np.int32(random.randint(-32, 32))

        b_src = None
        if (b_s == 0):
            b_src = np.uint32(random.randint(0, 32))
        else:
            b_src = np.int32(random.randint(-32, 32))

        dut.io_a.value  = int(a_src)
        dut.io_b.value  = int(b_src)
        dut.io_op.value = op
        dut.io_v.value  = 1
        await mlvp.ClockCycles(dut, 1)
        dut.io_v.value  = 0
        await mlvp.ClockCycles(dut, 10)

        res = dut.io_res.value
        expected = np.int64(a_src) * np.int64(b_src)

        # check
        print(f"op: {op:04b}, a: {a_src}, b: {b_src}, res: {res}, expected: {expected}")


def test_case1(request):
    dut = Multiplier(
        waveform_filename = "report/mul.fst",
        coverage_filename = "report/mul.dat"
    )
    dut.init_clock("clock")
    mlvp.run(mul_test(dut))
    dut.finalize()
    rp.set_line_coverage(request, "report/mul.dat")
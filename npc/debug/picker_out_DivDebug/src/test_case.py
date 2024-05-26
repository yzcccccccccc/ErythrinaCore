from src.Divisor import *

import mlvp.reporter as rp
import mlvp

import random
import numpy as np

async def mul_test(dut):
    print("\nop: 00 -> div, 01 -> divu, 10 -> mod, 11 -> modu\n")
    dut:Divisor = dut
    mlvp.create_task(mlvp.start_clock(dut))

    dut.reset.value = 1
    await mlvp.ClockCycles(dut, 10)
    dut.reset.value = 0
    await mlvp.ClockCycles(dut, 10)
    
    for i in range(10):
        op = 3
        issigned = op == 0 or op == 2
        '''
        if (issigned):
            a = np.int32(random.randint(-2 ** 31, 2 ** 31 - 1))
            b = np.int32(random.randint(-2 ** 31, 2 ** 31 - 1))
            if (b == 0):
                continue
        else:
            a = np.uint32(random.randint(0, 2 ** 32 - 1))
            b = np.uint32(random.randint(0, 2 ** 32 - 1))
            if (b == 0):
                continue
        '''
        a = -1
        b = 2

        dut.io_v.value = 1
        dut.io_a.value = int(a)
        dut.io_b.value = int(b)
        dut.io_op.value = op
        await mlvp.ClockCycles(dut, 1)
        dut.io_v.value = 0
        await mlvp.ClockCycles(dut, 60)

        # get res
        res = dut.io_res.value

        isdiv = op == 0 or op == 1
        expect_res = None
        if isdiv:
            expect_res = np.int32(a // b) if issigned else np.uint32(a // b)
        else:
            expect_res = np.int32(a % b) if issigned else np.uint32(a % b)
        expect_res = np.uint32(expect_res)
        print(f"op: {op:02b}, a: {a:#08x}, b: {b:#08x}, res: {res:#08x}, expect: {expect_res:#08x}")

        
def test_case1(request):
    dut = Divisor(
        waveform_filename = "report/div.fst",
        coverage_filename = "report/div.dat"
    )
    dut.init_clock("clock")
    mlvp.run(mul_test(dut))
    dut.finalize()
    rp.set_line_coverage(request, "report/div.dat")
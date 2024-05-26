import os
DUT_PATH = os.path.dirname(os.path.abspath(__file__)) + "/.."
os.sys.path.append(DUT_PATH)

from UT_MulDebug import *

class Multiplier(DUTMulDebug):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def finalize(self):
        super().finalize()
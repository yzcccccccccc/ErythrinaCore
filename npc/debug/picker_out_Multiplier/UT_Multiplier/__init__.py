
if __package__ or "." in __name__:
	from . import xspcomm as xsp
else:
	import xspcomm as xsp

if __package__ or "." in __name__:
	from .libUT_Multiplier import *
else:
	from libUT_Multiplier import *


class DUTMultiplier(DutUnifiedBase):

	# 初始化
	def __init__(self, waveform_filename=None, coverage_filename=None, *a, **kw):
		super().__init__(*a, **kw)
		self.xclock = xsp.XClock(self.step)
		self.port  = xsp.XPort()
		self.xclock.Add(self.port)
		self.event = self.xclock.getEvent()

		# set output files
		if waveform_filename:
			super().set_waveform(waveform_filename)
		if coverage_filename:
			super().set_coverage(coverage_filename)

		# all Pins
		self.clock = xsp.XPin(xsp.XData(0, xsp.XData.In), self.event)
		self.reset = xsp.XPin(xsp.XData(0, xsp.XData.In), self.event)
		self.io_a = xsp.XPin(xsp.XData(33, xsp.XData.In), self.event)
		self.io_b = xsp.XPin(xsp.XData(33, xsp.XData.In), self.event)
		self.io_regEnables_0 = xsp.XPin(xsp.XData(0, xsp.XData.In), self.event)
		self.io_regEnables_1 = xsp.XPin(xsp.XData(0, xsp.XData.In), self.event)
		self.io_res = xsp.XPin(xsp.XData(66, xsp.XData.Out), self.event)


		# BindDPI
		self.clock.BindDPIRW(DPIRclock, DPIWclock)
		self.reset.BindDPIRW(DPIRreset, DPIWreset)
		self.io_a.BindDPIRW(DPIRio_a, DPIWio_a)
		self.io_b.BindDPIRW(DPIRio_b, DPIWio_b)
		self.io_regEnables_0.BindDPIRW(DPIRio_regEnables_0, DPIWio_regEnables_0)
		self.io_regEnables_1.BindDPIRW(DPIRio_regEnables_1, DPIWio_regEnables_1)
		self.io_res.BindDPIRW(DPIRio_res, DPIWio_res)

		# Add2Port
		self.port.Add("clock", self.clock.xdata)
		self.port.Add("reset", self.reset.xdata)
		self.port.Add("io_a", self.io_a.xdata)
		self.port.Add("io_b", self.io_b.xdata)
		self.port.Add("io_regEnables_0", self.io_regEnables_0.xdata)
		self.port.Add("io_regEnables_1", self.io_regEnables_1.xdata)
		self.port.Add("io_res", self.io_res.xdata)


	def __del__(self):
		super().__del__()
		self.finalize()

	def init_clock(self,name:str):
		self.xclock.Add(self.port[name])

	def Step(self,i: int):
		return self.xclock.Step(i)

	def StepRis(self, call_back, args=(), kwargs={}):
		return self.xclock.StepRis(call_back, args, kwargs)

	def StepFal(self, call_back, args=(), kwargs={}):
		return self.xclock.StepFal(call_back, args, kwargs)

	def __getitem__(self, key):
		return xsp.XPin(self.port[key], self.event)

	async def astep(self,i: int):
		return await self.xclock.AStep(i)

	async def acondition(self,fc_cheker):
		return await self.xclock.ACondition(fc_cheker)

	def runstep(self,i: int):
		return self.xclock.RunStep(i)

if __name__=="__main__":
	dut=DUTMultiplier("libDPIMultiplier.so")
	dut.Step(1)

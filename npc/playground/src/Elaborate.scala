import top._
import circt.stage._

object Elaborate extends App {
  val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new ysyx_1919810))
  (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.SystemVerilog) :+ FirtoolOption("--disable-annotation-unknown"))
}

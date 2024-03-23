import ErythrinaCore._
import circt.stage._

object Elaborate extends App {
  val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new TOP))
  (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.SystemVerilog) :+ FirtoolOption("--disable-annotation-unknown"))
}

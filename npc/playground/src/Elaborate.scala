import ErythrinaCore._
import circt.stage._

object Elaborate extends App {
  def mytop       = new TOP()
  val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => mytop))
  (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
}

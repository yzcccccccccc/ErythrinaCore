import top._
import circt.stage._
import erythcore.fu.mul.MulDebug
import erythcore.fu.div.DivDebug

object Elaborate_Soc extends App {
  val firtoolOptions = Array("--lowering-options=" + List(
    // make yosys happy
    // see https://github.com/llvm/circt/blob/main/docs/VerilogGeneration.md
    "disallowLocalVariables",
    "disallowPackedArrays",
    "locationInfoStyle=wrapInAtSquareBracket"
  ).reduce(_ + "," + _))
  circt.stage.ChiselStage.emitSystemVerilogFile(new ysyx_1919810, args, firtoolOptions)
}

object Elaborate_Time extends App{
  val firtoolOptions = Array("--lowering-options=" + List(
    // make yosys happy
    // see https://github.com/llvm/circt/blob/main/docs/VerilogGeneration.md
    "disallowLocalVariables",
    "disallowPackedArrays",
    "locationInfoStyle=wrapInAtSquareBracket"
  ).reduce(_ + "," + _))
  
  circt.stage.ChiselStage.emitSystemVerilogFile(new TimingTop, args, firtoolOptions)
}

object Elaborate_Sim extends App{
  val firtoolOptions = Array("--lowering-options=" + List(
    // make yosys happy
    // see https://github.com/llvm/circt/blob/main/docs/VerilogGeneration.md
    "disallowLocalVariables",
    "disallowPackedArrays",
    "locationInfoStyle=wrapInAtSquareBracket"
  ).reduce(_ + "," + _))
  
  circt.stage.ChiselStage.emitSystemVerilogFile(new SimTop, args, firtoolOptions)
}

object Elaborate_Debug extends App{
  val firtoolOptions = Array("--lowering-options=" + List(
    // make yosys happy
    // see https://github.com/llvm/circt/blob/main/docs/VerilogGeneration.md
    "disallowLocalVariables",
    "disallowPackedArrays",
    "locationInfoStyle=wrapInAtSquareBracket"
  ).reduce(_ + "," + _))
  
  circt.stage.ChiselStage.emitSystemVerilogFile(new DivDebug, args, firtoolOptions)
}
  
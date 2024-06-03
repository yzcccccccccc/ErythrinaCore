package erythcore

import chisel3._
import chisel3.util._

object ExceptionSetting{
    def WIDTH   = 16

    def isUNI_idx   = 0 // Unimplemented Instruction
    def isEBK_idx   = 1 // Environment break
    //TODO: add more exception type
}
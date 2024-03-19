package ErythrinaCore

import chisel3._
import chisel3.util._

// Instruction Type
object InstrType extends Enumeration {
  type InstrType = UInt
  val TypeI, TypeR, TypeS, TypeU, TypeJ, TypeB = Value
}

// Using what functional unit (e.g. ALU, LSU, BRU...)
object OpeType extends Enumeration {
  type OpeType = UInt
  val ALU, LSU, BRU, MDU, CSR = Value
}

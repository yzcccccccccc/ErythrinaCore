package ErythrinaCore

import chisel3._
import chisel3.util._

// IDU!
class IDUIO extends Bundle with IDUtrait{
    val IFU2IDU = Flipped(Decoupled(new IF2IDzip))
    val IDU2EXU = Decoupled(new ID2EXzip)
}

class IDU extends Module with IDUtrait{
    val io = IO(new IDUIO)
}
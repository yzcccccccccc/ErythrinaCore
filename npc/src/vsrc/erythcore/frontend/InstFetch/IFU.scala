package erythcore.frontend.InstFetch

import chisel3._
import chisel3.util._
import erythcore._

import bus.ivybus._

class IFU extends Module with HasErythDefault{
    val io = IO(new Bundle {
        val mem_ports = new IvyBus

        val out = Decoupled(new InstFetchIO)
    })

    // FSM
    val sIDLE :: sREQ :: sRECV :: Nil = Enum(3)
    val state = RegInit(sIDLE)
    switch (state){
        is (sIDLE){
            when (~reset.asBool){
                state := sREQ
            }
        }
        is (sREQ){
            when (io.mem_ports.req.fire){
                state := sRECV
            }
        }
        is (sRECV){
            when (io.mem_ports.resp.fire){
                state := sIDLE
            }
        }
    }
}
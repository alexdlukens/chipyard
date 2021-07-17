package chipyard.fpgaperipherals

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, BaseModule}
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper.{HasRegMap, RegField}
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.gpio._

case class LEDParams(
  address: BigInt = 0x50000000,
  color: String = "red",
  width: Int = 1,
  header: String = "&gpio0 0",
  rgb: Boolean = false,
  number: Int = 0)

case object LEDKey extends Field[Option[LEDParams]](None)

trait LEDTopIO extends Bundle {
    val led_input = Input(Bool())
}

class LEDIO(val w: Int) extends Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
}


trait HasLEDIO extends BaseModule {
    val w: Int
    val io = IO(new LEDIO(w))
}

class LEDChiselModule(val w: Int) extends Module with HasLEDIO {
    
}

trait LEDModule extends HasRegMap{
    val io: LEDTopIO

    implicit val p: Parameters
    def params: LEDParams
    val clock: Clock
    val reset: Reset
    val impl = Module(new LEDChiselModule(params.width))

}


class LEDTL(params: LEDParams, beatBytes: Int)(implicit p: Parameters)
  extends TLRegisterRouter(
    params.address, "led0", Seq("sifive,gpio-leds"),
    beatBytes = beatBytes)(
      new TLRegBundle(params, _) with LEDTopIO)(
      new TLRegModule(params, _, _) with LEDModule){
        override def extraResources(resources: ResourceBindings) = Map(
            "label"      -> Seq(ResourceString("LD0red")),
            "gpios"          -> Seq(ResourceString("<&gpio0 0>")),
            "linux,default-trigger" -> Seq(ResourceString("none")))
      }
      


trait CanHavePeripheryGPIOLED {
    this: BaseSubsystem =>
    private val portName = "leds"

    val led = p(LEDKey) match {
        case Some(params) => {
            val led = LazyModule(new LEDTL(params, pbus.beatBytes)(p))
            pbus.toVariableWidthSlave(Some(portName)) { led.node }
            Some(led)
        }
        case None => None
    }
}

trait CanHavePeripheryGPIOLEDImp extends LazyModuleImp{
    val outer: CanHavePeripheryGPIOLED
}

class WithGPIOLEDs extends Config((site,here,up) => {
    case LEDKey => Some(LEDParams())
})
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
import testchipip._

case class LEDParams(
  address: BigInt = 0x0,
  color: String = "red",
  width: Int = 1,
  header: String = "&gpio0 0",
  rgb: Boolean = false,
  number: Int = 0)

case object LEDKey extends Field[Option[LEDParams]](None)




class LEDTL(params: LEDParams)(implicit p: Parameters) extends LazyModule
{
    val beatBytes = 8 
    val device = new SimpleDevice("led0", Seq("sifive,gpio-leds")){
        def extraResources(resources: ResourceBindings) = Map(
            "label"      -> Seq(ResourceString("LD0red")),
            "gpios"          -> Seq(ResourceString("<&gpio0 0>")),
            "linux,default-trigger" -> Seq(ResourceString("none")))
        override def describe(resources: ResourceBindings): Description = {
        val Description(name, mapping) = super.describe(resources)
        Description(name, mapping ++ extraResources(resources))
        }
    }
    
    val node = TLRegisterNode(
        address = Seq(AddressSet(0x1000, 0xff)),
        device = device,
        beatBytes = 8)

    lazy val module = new LazyModuleImp(this){
    }

    
}
    
    
      


trait CanHavePeripheryGPIOLED {
    this: BaseSubsystem =>
    private val portName = "leds"

    val led = p(LEDKey) match {
        case Some(params) => {
            val led = LazyModule(new LEDTL(params)(p))
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
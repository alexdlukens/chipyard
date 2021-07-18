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
  color: String = "red",
  header: String = "&gpio0",
  pinNumber: Int = 0,
  label: String = "LD0",
  rgb: Boolean = false)

case object LEDKey extends Field[Seq[LEDParams]](Nil)


object GPIOLEDs {
    private var index: Int = 0
}

class GPIOLEDs(params: LEDParams)(implicit p: Parameters) extends LazyModule with BindingScope
{
    val beatBytes = 8 
    val device = new SimpleDevice({"led@" + GPIOLEDs.index.toString}, Seq("sifive,gpio-leds")){
        GPIOLEDs.index = GPIOLEDs.index + 1
        def extraResources(resources: ResourceBindings) = Map(
            "label"      -> Seq(ResourceString(params.label + params.color)),
            "gpios"          -> Seq(ResourceString("<&gpio0 "+params.pinNumber.toString + ">")),
            "color"      -> Seq(ResourceString(params.color)),
            "linux,default-trigger" -> Seq(ResourceString("none")))
        override def describe(resources: ResourceBindings): Description = {
        val Description(name, mapping) = super.describe(resources)
        Description(name, mapping ++ extraResources(resources))
        }
        ResourceBinding {
            Resource(this, "exists").bind(ResourceString("true"))
        }

        
    }
    lazy val module = new LazyModuleImp(this){

    }

    
}
class LEDTL2(params: LEDParams)(implicit p: Parameters) extends LazyModule with BindingScope
{
    val beatBytes = 8 
    val device = new SimpleDevice("led", Seq("sifive,gpio-leds")){
        def extraResources(resources: ResourceBindings) = Map(
            "label"      -> Seq(ResourceString("LD0red")),
            "gpios"          -> Seq(ResourceString("<&gpio0 0>")),
            "linux,default-trigger" -> Seq(ResourceString("none")))
        override def describe(resources: ResourceBindings): Description = {
        val Description(name, mapping) = super.describe(resources)
        Description(name, mapping ++ extraResources(resources))
        }
        ResourceBinding {
            Resource(this, "").bind(ResourceInt(0x5000))
        }

        
    }
    
    
    lazy val module = new LazyModuleImp(this){

    }

    
}   
// works for 1 LED

// trait CanHavePeripheryGPIOLED {
//     this: BaseSubsystem =>
//     private val portName = "leds"

//     val led = p(LEDKey) match {
//         case Some(params) => {
//             val led = LazyModule(new GPIOLEDs(params)(p))
//             Some(led)
//         }
//         case None => None
//     }
// }      


trait CanHavePeripheryGPIOLED {
    this: BaseSubsystem =>

    val leds =Seq():+(p(LEDKey).foreach(params => {
        val led = LazyModule(new GPIOLEDs(params)(p))
        Some(led)
    }))
} 
trait CanHavePeripheryGPIOLEDImp extends LazyModuleImp{
    val outer: CanHavePeripheryGPIOLED
}

class WithGPIOLEDs extends Config((site,here,up) => {
    case LEDKey => (Seq(LEDParams(pinNumber = 1), LEDParams(color="green", pinNumber = 2), LEDParams(color="blue", pinNumber = 3)))
})
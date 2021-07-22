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

//Instantiate RGB LEDs as sub-devices under GPIO_0
class WithGPIOLEDs extends Config((site,here,up) => {
    case LEDKey => (Seq(LEDParams(color="red", label="LD0", pinNumber = 1), 
                        LEDParams(color="green", label="LD0", pinNumber = 2), 
                        LEDParams(color="blue", label="LD0", pinNumber = 3),
                        LEDParams(color="red", label="LD1", pinNumber = 19),
                        LEDParams(color="green", label="LD1", pinNumber = 21),
                        LEDParams(color="blue", label="LD1", pinNumber = 22),
                        LEDParams(color="red", label="LD2", pinNumber = 11),
                        LEDParams(color="green", label="LD2", pinNumber = 12),
                        LEDParams(color="blue", label="LD2", pinNumber = 13)))
})

// class WithGPIOButtons extends Config((site,here,up) => {

// })
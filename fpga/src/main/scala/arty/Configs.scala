// See LICENSE for license details.
package chipyard.fpga.arty

import sys.process._

import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink.{BootROMLocated}
import freechips.rocketchip.diplomacy.{DTSModel, DTSTimebase}
import freechips.rocketchip.system._
import freechips.rocketchip.tile._

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.spi._

import testchipip.{SerialTLKey}

import chipyard.{BuildSystem}
import chipyard.fpgaperipherals._

class WithDefaultPeripherals extends Config((site, here, up) => {
  case PeripheryUARTKey => List(
    UARTParams(address = 0x10013000),
    UARTParams(address = 0x10200000))
  case DTSTimebase => BigInt(32768)
  case JtagDTMKey => new JtagDTMConfig (
    idcodeVersion = 2,
    idcodePartNum = 0x000,
    idcodeManufId = 0x489,
    debugIdleCycles = 5)
  case SerialTLKey => None // remove serialized tl port
  case PeripherySPIFlashKey => List(
    SPIFlashParams(
      fAddress = 0x20000000,
      rAddress = 0x10014000,
      fSize = 0x1000000,
      defaultSampleDel = 3))
  case BootROMLocated(x) => up(BootROMLocated(x), site).map { p =>
    // invoke makefile for xip
    val make = s"make -C fpga/src/main/resources/arty/xip bin"
    require (make.! == 0, "Failed to build bootrom")
    p.copy(hang = 0x10000, contentFileName = s"./fpga/src/main/resources/arty/xip/build/xip.bin")
  }
})

// DOC include start: AbstractArty and Rocket
class WithArtyTweaks extends Config(
  new WithArtyGPIOHarnessBinder ++
  new chipyard.iobinders.WithGPIOIOCells ++
  new WithArtyJTAGHarnessBinder ++
  new WithArtyUARTHarnessBinder ++
  new WithArtyResetHarnessBinder ++
  new WithArtySPIFlashHarnessBinder ++
  new WithSPIFlashIOPassthrough ++
  new WithDebugResetPassthrough ++
  new WithDefaultPeripherals ++
  new freechips.rocketchip.subsystem.WithNBreakpoints(2))

class TinyRocketArtyConfig extends Config(
  new WithArtyTweaks ++
  new chipyard.TinyRocketConfig)

//WithPeripheryBusFrequency tells Chipyard to use a specific PBUS clock,
//without this config fragment, the Freedom-E-SDK does not know what frequency
//the fpga is running at, and it is harder to get correct serial output
class ArtyWithGPIOConfig extends Config(
  new chipyard.fpgaperipherals.WithGPIOButtons ++
  new chipyard.fpgaperipherals.WithGPIOLEDs ++
  new chipyard.config.WithRV64 ++
  new chipyard.config.WithGPIOIncludeIOF(true) ++
  new chipyard.config.WithGPIOWidth(32) ++
  new chipyard.config.WithGPIO ++
  new chipyard.config.WithPeripheryBusFrequency(65.0) ++
  new TinyRocketArtyConfig
)
// DOC include end: AbstractArty and Rocket

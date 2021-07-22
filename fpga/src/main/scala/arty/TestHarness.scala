package chipyard.fpga.arty

import chisel3._

import freechips.rocketchip.diplomacy.{LazyModule}
import freechips.rocketchip.config.{Parameters}

import sifive.fpgashells.shell.xilinx.artyshell.{ArtyShell}

import chipyard.{BuildTop, HasHarnessSignalReferences, HasTestHarnessFunctions}
import chipyard.harness.{ApplyHarnessBinders}
import chipyard.iobinders.{HasIOBinders}

class ArtyFPGATestHarness(override implicit val p: Parameters) extends ArtyShell with HasHarnessSignalReferences {

  val lazyDut = LazyModule(p(BuildTop)(p)).suggestName("chiptop")

  // Convert harness resets from Bool to Reset type.
  val hReset = Wire(Reset())
  hReset := ck_rst

  val dReset = Wire(AsyncReset())
  dReset := reset_core.asAsyncReset

  // default to 65MHz clock
  withClockAndReset(clock_65MHz, hReset) {
    val dut = Module(lazyDut.module)
  }

  val harnessClock = clock_65MHz
  val harnessReset = hReset
  val success = false.B

  val dutReset = dReset

  // must be after HasHarnessSignalReferences assignments
  lazyDut match { case d: HasTestHarnessFunctions =>
    d.harnessFunctions.foreach(_(this))
  }
  lazyDut match { case d: HasIOBinders =>
    ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
}


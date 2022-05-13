package net.liplum

import mindustry.ui.dialogs.BaseDialog

object ScalaDriver {
  def install(): Unit = {
    new BaseDialog("Scala Driver") {
      cont.add("Scala Installing...")
      addCloseButton()
    }.show()
  }
}

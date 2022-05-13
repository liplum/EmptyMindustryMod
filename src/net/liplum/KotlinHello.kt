package net.liplum

import mindustry.ui.dialogs.BaseDialog

object KotlinHello {
    fun hello() {
        BaseDialog("Hello Kotlin").apply {
            cont.add("Hellp Kotlin!")
            addCloseButton()
        }.show()
    }
}
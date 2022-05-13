package net.liplum

import arc.Events
import mindustry.game.EventType
import mindustry.mod.Mod
import mindustry.ui.dialogs.BaseDialog

class EmptyMod extends Mod {
    @Override
    void init() {
        Events.on(EventType.ClientLoadEvent.class) {
            (new BaseDialog("This is an empty mod").with {
                it.cont.add("You wasted 26.72MB to download this.").row()
                cont.button("Kotlin") {
                    KotlinHello.INSTANCE.hello()
                }.width(100f).row()
                cont.button("Scala") {
                    ScalaDriver.install()
                }.width(100f).row()
                cont.button("Java") {
                    JavaJava.java()
                }.width(100f).row()
                it.addCloseButton()
                it.show()
            })
        }
    }
}

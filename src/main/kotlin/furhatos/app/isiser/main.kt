package furhatos.app.isiser

import furhatos.app.isiser.flow.*
import furhatos.app.isiser.setting.GUIEvent
import furhatos.app.isiser.setting.module
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import furhatos.skills.RemoteGUI
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import furhatos.event.EventSystem

class IsiserSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}
fun main(args: Array<String>) {

    RemoteGUI(
        "GUI",
        hostname = "http://localhost:1234",
        port = 1234
    )
    //hostname = "http://${Skill.getFurhatWebAddress()}:1234",
    embeddedServer(Netty, port = 1234, module = Application::module).start()
    println("Starting skill...")


    Skill.main(args)
}

package furhatos.app.isiser

import furhatos.app.isiser.flow.*
import furhatos.app.isiser.handlers.module
import furhatos.app.isiser.setting.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import furhatos.skills.RemoteGUI
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class IsiserSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}
fun main(args: Array<String>) {
    RemoteGUI(
        GUI,
        hostname = GUI_HOSTNAME + GUI_PORT,
        port = GUI_PORT
    )
    //hostname = "http://${Skill.getFurhatWebAddress()}:1234",
    embeddedServer(Netty, port = GUI_PORT, module = Application::module).start()
    println("[ISISER] Starting skill...")

    Skill.main(args)
}

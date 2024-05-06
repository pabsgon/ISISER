package furhatos.app.isiser.flow.main

import furhatos.gestures.BasicParams
import furhatos.gestures.defineGesture

val CloseEyes = defineGesture("CloseEyes") {
    frame(0.4, persist = true) {
        BasicParams.BLINK_RIGHT to 1.0
        BasicParams.BLINK_LEFT to 1.0
    }
}
val OpenEyes = defineGesture("OpenEyes") {
    frame(0.4) {
        BasicParams.BLINK_RIGHT to 0.0
        BasicParams.BLINK_LEFT to 0.0
    }
}
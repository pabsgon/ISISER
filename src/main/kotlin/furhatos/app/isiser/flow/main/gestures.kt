package furhatos.app.isiser.flow.main

import furhatos.gestures.ARKitParams
import furhatos.gestures.BasicParams
import furhatos.gestures.Gestures
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
val VerySubtleShake = defineGesture("VerySubtleShake") {
    // Start with the head at the initial position
    frame(0.0, 0.5) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_PAN to 2.5
    }
    frame(0.5, 1.0) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_PAN to -2.0
    }
    reset(1.5)
}
val SubtleShake = defineGesture("SubtleShake") {
    // Start with the head at the initial position
    frame(0.0, 0.3) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_PAN to 4.0
    }
    frame(0.4, 0.6) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_PAN to -3.0
    }
    frame(0.6, 0.8) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_PAN to 1.5
    }
    reset(1.5)
}

val SubtleNod = defineGesture("SubtleNod") {
    // Start with the head at the initial position
    frame(0.0, 0.3) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 4.0
    }
    frame(0.4, 0.6) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to -3.0
    }
    frame(0.6, 0.8) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 1.5
    }

    reset(1.5)
}

val VerySubtleWobble = defineGesture("VerySubtleWobble") {
    // Start with the head at the initial position
    frame(0.0, 0.3) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 4.0
        BasicParams.NECK_ROLL to 4.0
    }
    frame(0.4, 0.6) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to -3.0
        BasicParams.NECK_ROLL to -3.0
    }
    frame(0.6, 0.8) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 1.5
        BasicParams.NECK_ROLL to 1.5
    }

    reset(1.5)
}

val MySmile = defineGesture("MySmile") {
    frame(0.32, 0.72) {
        BasicParams.SMILE_CLOSED to 0.5
    }
    frame(0.2, 0.72){
        BasicParams.BROW_UP_LEFT to 1.0
        BasicParams.BROW_UP_RIGHT to 1.0
    }
    frame(0.16, 0.72){
        BasicParams.BLINK_LEFT to 0.1
        BasicParams.BLINK_RIGHT to 0.1
    }
    reset(1.04)
}
val PauseUncertain = defineGesture("PauseUncertain") {
    // Frame to open eyes and raise eyebrows
    frame(0.0, 0.4, persist = true) {
        BasicParams.NECK_PAN to 2.5
        BasicParams.NECK_TILT to 1
    }
    frame(0.0, 0.7) {
        ARKitParams.EYE_LOOK_DOWN_LEFT to 1.0
        ARKitParams.EYE_LOOK_DOWN_RIGHT to 1.0
        ARKitParams.EYE_LOOK_OUT_LEFT to 1.0
        ARKitParams.EYE_LOOK_IN_RIGHT to 1.0
    }
    frame(0.4, 0.7, persist = true) {
        ARKitParams.MOUTH_LEFT to 0.3
    }

    frame(0.7, 1.0, persist=true) {
        ARKitParams.EYE_LOOK_DOWN_LEFT to 0.5
        ARKitParams.EYE_LOOK_DOWN_RIGHT to 0.5
        ARKitParams.EYE_LOOK_OUT_LEFT to 0.5
        ARKitParams.EYE_LOOK_IN_RIGHT to 0.5
    }
    frame(0.4, 0.7, persist = true) {
        BasicParams.NECK_PAN to 1.0
        BasicParams.NECK_TILT to 0.5
    }

    // Reset to ensure all actuators return to their default state
    reset(1.6)
}

val PauseCertain = defineGesture("EyesOpenBrowUpNod") {
    // Frame to open eyes and raise eyebrows
    frame(0.0, 0.6, persist=true) {
        ARKitParams.EYE_WIDE_LEFT to 1.2
        ARKitParams.EYE_WIDE_RIGHT to 1.2
        BasicParams.BROW_UP_LEFT to 1.2
        BasicParams.BROW_UP_RIGHT to 1.2
    }
    // Frame to perform the subtle nod
    frame(0.3, 0.6) {
        BasicParams.NECK_TILT to -0.3
    }
    frame(0.0, 0.1) {
        /*ARKitParams.MOUTH_UPPER_UP_LEFT to 1*/
        ARKitParams.CHEEK_SQUINT_LEFT to 0.3
        ARKitParams.CHEEK_SQUINT_RIGHT to 0.3
    }
    frame(0.4, 0.7) {
        /*ARKitParams.MOUTH_UPPER_UP_LEFT to 1*/
        ARKitParams.MOUTH_PRESS_LEFT to 0.3
        ARKitParams.MOUTH_PRESS_RIGHT to 0.3
    }
    frame(0.7, 0.8) {
        /*ARKitParams.MOUTH_UPPER_UP_LEFT to 1*/
        ARKitParams.CHEEK_SQUINT_LEFT to 0.3
        ARKitParams.CHEEK_SQUINT_RIGHT to 0.3
    }
    // Frame to return head to initial position
    frame(0.4, 0.6) {
        BasicParams.NECK_TILT to 0.1
    }
    frame(0.6, 0.9) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 3.0
    }
    frame(0.9, 1.1) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to -2.0
    }
    frame(1.1, 1.4) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 1.2
    }

    // Reset to ensure all actuators return to their default state
    reset(1.6)
}
val SubtleWobble = defineGesture("SubtleWobble") {
    // Start by tilting head to the left
    frame(0.1, 1.75, persist = true) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 4.0
        BasicParams.NECK_TILT to 4.0
    }
    frame(0.0, 0.4) {
        BasicParams.NECK_ROLL to -4.5
    }
    // Tilt head to the right
    frame(0.4, 0.8) {
        BasicParams.NECK_ROLL to 4
    }
    // Return head to the left
    frame(0.8, 1.2) {
        BasicParams.NECK_ROLL to -3.5
    }
    // Tilt head to the right
    frame(1.2, 1.6) {
        BasicParams.NECK_ROLL to 3
    }
    // Return head to the center position
    frame(1.6, 1.8) {
        BasicParams.NECK_ROLL to 0.0
    }
    // Reset to ensure all actuators return to their default state
    reset(1.8)
}
val SubtleWobbleYes = defineGesture("SubtleWobbleYes") {
    // Start by tilting head to the left
    frame(0.1, 1.75, persist = true) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 4.0
        BasicParams.NECK_TILT to 4.0
    }
    frame(0.0, 0.4) {
        BasicParams.NECK_ROLL to -4.5
    }
    // Tilt head to the right
    frame(0.4, 0.8) {
        BasicParams.NECK_ROLL to 4
    }
    // Return head to the left
    frame(0.8, 1.2) {
        BasicParams.NECK_ROLL to -3.5
    }
    frame(0.3, 1.3, persist = true) {
        ARKitParams.MOUTH_SMILE_LEFT to 0.5
        ARKitParams.MOUTH_SMILE_RIGHT to 0.5
    }
    // Tilt head to the right
    frame(1.2, 1.6) {
        BasicParams.NECK_ROLL to 3
    }
    // Return head to the center position
    frame(1.6, 1.8) {
        BasicParams.NECK_ROLL to 0.0
    }
    // Reset to ensure all actuators return to their default state
    reset(1.8)
}
val SubtleWobbleNo = defineGesture("SubtleWobbleNo") {
    // Start by tilting head to the left
    frame(0.1, 1.75, persist = true) {
        // Move head down slightly (e.g., 3 cm)
        BasicParams.NECK_TILT to 4.0
        BasicParams.NECK_TILT to 4.0
    }
    frame(0.0, 0.4) {
        BasicParams.NECK_ROLL to -4.5
    }
    // Tilt head to the right
    frame(0.4, 0.8) {
        BasicParams.NECK_ROLL to 4
    }
    frame(0.3, 1.0, persist = true) {
        ARKitParams.MOUTH_LEFT to 0.3
    }
    // Return head to the left
    frame(0.8, 1.2) {
        BasicParams.NECK_ROLL to -3.5
    }
    // Tilt head to the right
    frame(1.2, 1.6) {
        BasicParams.NECK_ROLL to 3
    }
    // Return head to the center position
    frame(1.6, 1.8) {
        BasicParams.NECK_ROLL to 0.0
    }
    // Reset to ensure all actuators return to their default state
    reset(1.8)
}
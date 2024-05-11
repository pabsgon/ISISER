package furhatos.app.isiser.flow.main

import furhatos.flow.kotlin.NullSafeUserDataDelegate
import furhatos.records.User

var User.qNum by NullSafeUserDataDelegate { 0 }


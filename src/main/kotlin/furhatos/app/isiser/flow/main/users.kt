package furhatos.app.isiser.flow.main

import furhatos.app.isiser.nlu.FruitList
import furhatos.flow.kotlin.NullSafeUserDataDelegate
import furhatos.records.User

class FruitData {
    var fruits : FruitList = FruitList()
}
var User.qNum by NullSafeUserDataDelegate { 0 }

val User.order : FruitData
    get() = data.getOrPut(FruitData::class.qualifiedName, FruitData())

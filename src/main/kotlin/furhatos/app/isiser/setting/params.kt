package furhatos.app.isiser.setting
const val SOURCEDATA_SPREADSHEETID = "1--UZgR4W01c7Z5yml06KCWw4xm8hRBJLVv-bT63iyzg" // Your spreadsheet ID
const val SOURCEDATA_RANGE = "RAWDATA_FOR_LOADING!A3:AS250"
const val SOURCEDATA_CREDENTIAL_FILE_PATH= "C:\\Development\\ISISER\\Isiser\\src\\main\\resources\\json\\isiser00-4078fd895f22.json" // Adjust this path
const val GUI_HOSTNAME = "http://localhost:"
const val GUI_PORT = 8888
const val MAX_NUM_USERS = 1
const val ENGAGMENT_DISTANCE = 1.5
const val CERTAIN_ROBOT_SPEECH_RATE = 1.15
const val UNCERTAIN_ROBOT_SPEECH_RATE = 0.85
const val SILENT_MILLISECS_PER_DOT = 250
const val LOG_ALL_EVENTS = false // If true, all events will be logged. Otherwise, only the GUI, dialog and gesture events will be logged.
const val MAX_NUM_BACKCHANNEL_WORDS = 2
const val MAX_BACKCHANNEL_LENGTH = 4000 //Milliseconds.
const val MAX_NUM_PROBES_AT_PERSUASION = 1
const val MAX_NUM_PROBES_AT_REVIEW = 1
const val WAITING_TIME_MILLISECS = 8000


const val USE_PROBES_AT_DISCUSSION = true /* If this is true, in Persuasion and Review, when the user denies, a number of probes will be
posed (either MAX_NUM_PROBES_AT_PERSUASION or MAX_NUM_PROBES_AT_REVIEW); once reached, it will continue with claims.
If false, it will always proceed with claims.*/

const val PRO = 0
const val DEV = 1
const val TEST_INTENTS = 2
const val TEST_UTTERANCES = 3
const val APP_EXECUTION_MODE = DEV





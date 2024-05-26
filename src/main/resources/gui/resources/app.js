const DEV_MOD1 = 0 // This does not go to the server
const DEV_MOD2 = 1 // THis will run only q 7 and 8 
const DEV_MOD_CHOOSING = 9 // THis will run from state (1 to 8)

const ADM_MODE = 10 // This wil show the questions on the right hand side
const PRO_MODE = 99

const APP_MODE = ADM_MODE
const LANG_EN = "EN"
const LANG_SE = "SE"
const NUMBER_STUDENTS = 62
const LANG = LANG_EN
const IMAGES_PATH = "resources/images/"
const ANSWER_TRUE = "TRUE"
const ANSWER_FALSE = "FALSE"
const URL_VARS = getUrlVars()
const readyButton = document.getElementById('readyBut')
const falseButton = document.getElementById('falseBut')
const trueButton = document.getElementById('trueBut')
const confirmButton = document.getElementById('confirmButton');

const STAGES = [0,0.2,1,2,3,4,5,6,7,8,0.3]
const FROM_N_STAGES = [0,0.2,2,3,4,5,6,7,8,0.3]
const LAST2_STAGES = [0,0.2,7,8,0.3]
const STAGE_1ST_QUESTION = 1
const EVTYPE_SYNCH_REQUESTED = "SYNCH_REQUESTED"
const EVTYPE_NEW_STAGE_REQUESTED = "NEW_STAGE_REQUESTED"
const EVTYPE_ALLOW_ANSWER_CHANGE_REQUESTED = "ALLOW_ANSWER_CHANGE_REQUESTED"
const EVTYPE_ALLOW_CONFIRMATION_REQUESTED = "ALLOW_CONFIRMATION_REQUESTED"
const EVTYPE_ANSWER_MARKED = "ANSWER_MARKED"	
const QUESTION_CHANGE_TIME = 3000 //millisecs until the new problem is shown
const SYNCH_TIME_INTERVAL = 3000 //millisecs between recurrent requests to the server until success.
const ALLOW_CHANGE_ANSWER_TIMEOUT = 5000 //millisecs between recurrent requests to the server until success.

const QSTAGEMODE_REFLECTION = 0
const QSTAGEMODE_DISCLOSURE = 1
const QSTAGEMODE_DISCUSSION = 2
const QSTAGEMODE_DECISION = 3


// The first time, the page will always try to synch with the server, in case the webpage closed.
// The server will return the stage and user. The webpage will set these two params, showing the correct stage.

/* ............... STAGES .......................*/ 
var allowAnswerChangeTimeout, synchTimeout;
var stages = APP_MODE==DEV_MOD2?LAST2_STAGES:(APP_MODE==DEV_MOD_CHOOSING?FROM_N_STAGES:STAGES)
stages.curPos = -1
stages.currentMode = null
stages.next = function(){return this[this.curPos+1]}
stages.set = function(st){
	st = parseFloat(st)
	if(this.includes(st)){
		if(st!=this.current){
			this.curPos = this.indexOf(st)
			this.previous = this.current
			this.current=st
			return true
		}
	}else{
		return this.init()
	}
}
stages.init = function(){
	if(this.curPos<0){
		this.curPos=0
		this.previous=null
		this.current=this[0]
		return true
	}
}
var fakeServer = APP_MODE == DEV_MOD2 ? LAST2_STAGES : (APP_MODE == DEV_MOD_CHOOSING ? FROM_N_STAGES : STAGES);
fakeServer.stage = fakeServer[0]; // Corrected 'this' to 'fakeServer'
fakeServer.mode = 0;
fakeServer.subject = "";
fakeServer.status = 0;
fakeServer.contact = function (req) {
    switch (req.type) {
        case EVTYPE_NEW_STAGE_REQUESTED:
            this.status = 0;
            this.stage = req.stage;
            this.subject = req.subject;
            this.mode = 0;
            break;
        case EVTYPE_SYNCH_REQUESTED:
			if(this.subject==""){
				this.status = 0
			}else{	
				var randomNumber = Math.floor(Math.random() * 4);

				// Adjust the mode based on the random number
				if (randomNumber === 0) {
					if (this.mode <3) this.mode++;
				}
			}
            break;
        case EVTYPE_ANSWER_MARKED:
            this.status = 0
			if(this.mode==0)this.mode = 1
			break;
		default:
			console.warn("Unknown mode:", mode);
	}
	
	return {status:"" + this.status,
			subject:"" + this.subject,
				stage:"" + this.stage,
				mode:"" + this.mode}
}
/* ...............................................*/ 
var data = []
var answer, subjectId, selectedSubjectButton
var questionStageMode = null
var dict,qs;
data["EN"]={"project": "ISISER-SOLNA Project [EN]","title": "Electrical circuits","sendingToFurhat": "Sending to Furhat: Question ","instructions": "Instructions","instruction1": "You have to answer 8 questions and you get to use the robot to help you decide on the right answer.","instruction2": "You must first answer what you think.","instruction3": "You may change your mind after the robot describes its view of the problem","instruction4": "When you have decided, press Confirm.","instruction5": "Talk with Furhat until you are both ready","gettingReady": "Get ready.","instruction6": "Check with Furhat that he is ready","weAreReady": "We are ready","termQuestion": "Question","termConfirm": "Confirm","termTrue": "True ","termFalse": "False ","termNextInstruction": "Next instruction","farewell": "Ok, that was it!","instruction0": "Select the participant number you received in the confirmation email. Press again to confirm.","questions": ["qOriginalId|qStatement|qImage","10|When the lamps are connected in series, all the lamps will glow more dimly when another lamp is connected.|question10.png","20|The blue lamp in C2 shines brighter than the blue lamp in C1.|question20.png","6|If the switch is turned on, the blue light will shine brighter than the red and yellow lights|question8.png","17|The blue lights in C1 and C2 consume the same amount of power.|question17.png","16|The green light shines as brightly as the other three.|question16.png","21|The blue lamp in C2 shines brighter than the one in C1.|question21.png","8|If the switch is connected, the yellow light could not be brighter than now|question4_5_6_7.png","9|The blue lamp in C1 consumes less power than the blue lamp in C2|question9.png"]}				
data["SE"]={"project": "ISISER-SOLNA Project [SE]","title": "Elektriska kretsar","sendingToFurhat": "Skickar till Furhat: Fråga","instructions": "Instruktioner","instruction1": "Du ska svara på 8 frågor och du får ta roboten till hjälp för att bestämma dig för rätt svar.","instruction3": "Du får ändra dig efter att roboten beskrivit sin bild av problemet","instruction4": "När du bestämt dig trycker du på Bekräfta.","instruction5": "Nu kan du börja prata med Furhat. Säg Hej, så vaknar han.","gettingReady": "Gör er klara.","instruction6": "Kolla med Furhat att han är redo","weAreReady": "Vi är klara","termQuestion": "Fråga","termConfirm": "Bekräfta","termTrue": "Sant","termFalse": "Falskt","termNextInstruction": "Nästa instruktion","farewell": "Det var allt!","instruction0": "Välj den deltagarsiffra du fick i bekräftelse-mejlet. Tryck igen för att bekräfta.","questions": ["qOriginalId|qStatement|qImage","10|När lamporna är kopplade i serie kommer alla lampor att lysa svagare när en till lampa kopplas in.|question10.png","20|Den blå lampan i C2 lyser starkare än den blå lampan i C1.|question20.png","6|Om strömbrytaren slås till kommer den blå lampan lysa starkare än den röda och gula lampan|question8.png","17|De blå lamporna i C1 och C2 förbrukar lika mycket effekt.|question17.png","16|Den gröna lampan lyser lika starkt som de andra tre.|question16.png","21|Den blå lampan i C2 lyser starkare än den i C1.|question21.png","8|If the switch is connected, the yellow light could not be brighter than now|question4_5_6_7.png","9|Den blå lampan i C1 förbrukar mindre effekt än den blå lampan i C2|question9.png"]}
// app.js

function sendAnswer(){
	var req = {"message": "ANSWER[" + answer + "] [" + stages.current + "] [" + subjectId + "] SENT ..",
				"type": EVTYPE_ANSWER_MARKED,
				"stage": stages.current,
				"subject": subjectId,
				"answer": answer}
	contactServer(req)
}
function requestStage(st){
	var req = {"message": "STAGE[" + st + "] [" + subjectId + "] REQUESTED ..",
				"type": EVTYPE_NEW_STAGE_REQUESTED,
				"stage": st,
				"subject": subjectId,
				"answer": answer}
	contactServer(req)
}

function synch(){
	var req = {"message": "SYNCH REQUESTED ..",
				"type": EVTYPE_SYNCH_REQUESTED,
				"subject": subjectId}
	contactServer(req)
}

function trySynch(){
	console.log("trying to synch")
	clearTimeout(synchTimeout)
	synchTimeout = window.setTimeout(function(){synch()},SYNCH_TIME_INTERVAL)
}
function contactServer(req) {
	if(APP_MODE<DEV_MOD2){
		var retData = fakeServer.contact(req)
		if(!retData || retData.status!="0"){
			console.log("Server response was not ok. Retrying...")
			/*clearTimeout(synchTimeout)
			synchTimeout = window.setTimeout(function(){contactServer(req)},SYNCH_TIME_INTERVAL)*/
		}else{
			console.log("Server response ok.  data=" + JSON.stringify(retData))
			if(req.type != EVTYPE_ANSWER_MARKED){
				changeSubject(retData.subject)
				changeStage(retData.stage, retData.mode)
			}
		}
	} else{
		doContactServer(req)
	}

}
function doContactServer(req) {
	console.log("Contactig server with req=[" + JSON.stringify(req) + "]")
	
    fetch('/receive', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(req)
    }).then(response => {
        if (!response.ok) { 		 	
            throw new Error('[ISISER] Network response was not ok');
        }
        return response.json();  //Only parse as JSON if the response is OK
    })
    .then(data => {
		console.log("Receiving from server data=" + JSON.stringify(data))
		/*if(!data){
			console.error('[SERVER ERR]:', data.error);
			return 
		}else{
			if(!data.error){
				console.error('[ISISER ERR] Server App failed.')
				return
			}else{
				if(data.status!="0"){
					console.error('[ISISER ERR] Server App did not allow the request.')
					return
				}else{
					console.error('[ISISER OK] Server App did allow the request.')
					clearTimeout(synchTimeout)
				}
			}
		}*/
		if(data.error){
			console.error('Server error:', data.error);
		}else{
			if(!data || data.status!="0"){
				console.error('[ISISER] Server did not allow the request.')
				return
			}else{
				if(req.type != EVTYPE_ANSWER_MARKED){
					changeSubject(data.subject)
					changeStage(data.stage, data.mode)
				}
				return data
				//changeSubject(data.subject)
				//changeStage(data.stage)
			}
		}
	})
    .catch(error => {
		console.error('[ISISER] Error::', error)
		return null
	});
}


function setQuestionStage(mode) {
	console.log("setQuestionStage: current " + stages.current + " > new mode=" +  mode)
	//console.log("setQuestionStage: QSTAGEMODE_REFLECTION == mode? ->" +  (QSTAGEMODE_REFLECTION == mode) )
	if(stages.current >= STAGE_1ST_QUESTION){
		if (mode!=null && stages.currentMode != mode) {
			stages.currentMode= mode
			if(mode==QSTAGEMODE_REFLECTION){
						console.log("Mode REFLECTION set")
						falseButton.enable();
						trueButton.enable();
						confirmButton.disable();
						setAnswer(); 
			}else{
				if(mode==QSTAGEMODE_DISCLOSURE){
						console.log("Mode DISCLOSURE set")
						falseButton.disable();
						trueButton.disable();
						confirmButton.disable();
						trySynch()
				}else{
					if(mode== QSTAGEMODE_DISCUSSION){
						console.log("Mode DISCUSSION set")
						falseButton.enable();
						trueButton.enable();
						confirmButton.disable();
						trySynch()
					}else{
						if(mode==QSTAGEMODE_DECISION){
							console.log("Mode DECISION set")
							falseButton.enable();
							trueButton.enable();
							confirmButton.enable();
						}else{
							console.warn("Unknown mode:", mode);
						}
					}
				}
			}
			
		}else{
			console.log("*mode is null or same as before")
			trySynch()
		}
	}
}
function clickAnswerButton(el, ans){
	if(el.isEnabled()){setAnswer(ans)}
}

function setAnswer(ans){
	if(!ans){//this means to clear all answers
		answer = null
		falseButton.switchOff()
		trueButton.switchOff()
	}else{
		
		if(ans != answer){
			if(ans==ANSWER_TRUE){
				trueButton.switchOn();
				falseButton.switchOff()
			}else{
				falseButton.switchOn();
				trueButton.switchOff()
			}
			answer = ans
			sendAnswer()
			if(stages.currentMode==QSTAGEMODE_REFLECTION){
				allowAnswerChangeTimeout = window.setTimeout(function(){setQuestionStage(QSTAGEMODE_DISCLOSURE)},ALLOW_CHANGE_ANSWER_TIMEOUT)
			}
		}
	}
}
function setSubjectId(){
	var elm = this
	if(elm){
		if(elm != selectedSubjectButton){
			if(selectedSubjectButton)selectedSubjectButton.switchOff()
			elm.switchOn()
			selectedSubjectButton = elm
		}else{
			elm.style.background ="#f5cb13"
			changeSubject(elm.dataset["num"])
			tryNextStage()
		}
	}
}

function changeStage(st, mode){
	console.log("Current stage [" + stages.current + "] Trying to changing stage [" + st + "]")
	if(stages.set(st) ){
		st = stages.current
		console.log('slide_'+ (stages.previous<1?stages.previous:"Q"))
		if(stages.previous!=null)document.getElementById('slide_'+ (stages.previous<1?stages.previous:"Q")).style.display = "none"
		if(st >= STAGE_1ST_QUESTION){
			document.getElementById('loadingQnum').innerHTML = st
			document.getElementById('qNum').innerHTML = st
			document.getElementById('qStatement').innerHTML = qs[st].qStatement
			document.getElementById('qImage').src = IMAGES_PATH + qs[st].qImage
			setQuestionStage(QSTAGEMODE_REFLECTION)
			window.setTimeout(function(){document.getElementById('slide_'+ (st<1?st:"Q")).style.display = "flex"},QUESTION_CHANGE_TIME)
		}else{
			document.getElementById('slide_'+ (st<1?st:"Q")).style.display = "flex"
			if(st==0.2)window.setTimeout(function(){document.getElementById('readyBut').style.visibility="visible"},3000)
		}
	}else{
		setQuestionStage(mode)
		console.log("Stage not changed.")
	}
}

function changeSubject(subjId){
	console.warn("Changing the subject to:" + subjId)
	document.getElementById('userNum').innerHTML=subjId
	subjectId = subjId
}

function tryChangeStage(st){
	st = parseFloat(st)
	st = stages.includes(st)?st:undefined
	/*if(APP_MODE<DEV_MOD2){
		changeStage(st)
	}else{//requesting stage to the server
		requestStage(st)
	}*/
	requestStage(st)
}
function requestNextStage(btn){
	if(btn.isEnabled()){tryNextStage()}
}

function tryNextStage(){
	console.log("tryNextStage() called")
	tryChangeStage(stages.next())
}



function loadTexts(){
	dict = data[LANG]
	qs = dict.questions
	for(var i=0;i<qs.length;i++){
		qs[i] = qs[i].split("|")
	}

	var labels = qs[0]
	for(var i=1;i<qs.length;i++){
		var q = {}
		for(var j=0;j<labels.length;j++){
			q[labels[j]] = qs[i][j]
		}
		qs[i] = q
	}

	var elems = document.querySelectorAll('[data-id]');
	for(var i=0;i<elems.length;i++){
		elems[i].innerHTML = dict[elems[i].dataset['id']]
	}
}
function getUrlVars(){
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}
function createNumButtons(){
	var numRows = Math.round(Math.sqrt(NUMBER_STUDENTS))
	var butsPerRow = numRows*numRows<NUMBER_STUDENTS?numRows:numRows-1
	console.log("numRows=" + numRows)
	console.log("butsPerRow=" + butsPerRow)
	var looseNums = NUMBER_STUDENTS - numRows*butsPerRow
	console.log("looseNums=" + looseNums)
	console.log("numRows=" + numRows)

	var holderBase = document.querySelector(".numberButtonsWrapper");
	var frame = holderBase.parentElement
	var butBase = document.getElementById("subject");

	function cloneEl(num){
		elnew = butBase.cloneNode(true);
		elnew.id= butBase.id + num
		elnew.dataset["num"] = num
		elnew.innerHTML = num
		return elnew
	}
	var count = 0
	for(var i=0;i<numRows;i++){
		var holder = holderBase.cloneNode(true);
		frame.appendChild(holder)
		for(var j=0;j<butsPerRow;j++){
			count++			
			holder.appendChild(cloneEl(count))
		}
		if(looseNums){
			count++
			holder.appendChild(cloneEl(count))
			looseNums--
		}
	}
	holderBase.remove()
	butBase.remove()
}
function setUpButtons(){
	var disable = function() {
		this.classList.replace('enabled', 'disabled');
	};

	var isEnabled = function() {
		return this.classList.contains('enabled');
	};

	var enable = function() {
		this.classList.replace('disabled', 'enabled');
	};

	var isActive = function(){
		return this.classList.contains("active")
	}
	var switchOn = function(){
		//this.classList.add('active');
		if(!this.isActive())this.classList.toggle('active')
	}
	var switchOff = function(){
		//this.classList.remove('active');
		if(this.isActive())this.classList.toggle('active')
	}
	function setupButton(but){
		but.isActive = isActive
		but.switchOff = switchOff
		but.switchOn = switchOn
		but.enable = enable
		but.disable= disable
		but.isEnabled= isEnabled
	}
	
	
	var allButtons = document.querySelectorAll(".button")
	for(var i=0;i<allButtons.length;i++){
		setupButton(allButtons[i])
	}
	setupButton(confirmButton)
	var elems = document.querySelectorAll('[data-num]');
	for(var i=0;i<elems.length;i++){
		elems[i].onclick = setSubjectId
	}
}
function loadElements(){
	createNumButtons()
}

function init(){
	if(APP_MODE<PRO_MODE)document.getElementById('devmode').style.display="block"
	loadElements()
	loadTexts()
	if(URL_VARS.admin){
		if(APP_MODE<PRO_MODE)document.getElementById('admin').style.display="block"
	}
	setUpButtons()
	synch()
	//tryChangeStage()
}
/* ----------------------------- INIT ------------------------------ */
init()

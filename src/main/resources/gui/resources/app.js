const DEV_MODE = true

const LANG = "SE"
const IMAGES_PATH = "resources/images/"
const ANSWER_TRUE = "TRUE"
const ANSWER_FALSE = "FALSE"
const URL_VARS = getUrlVars()
const readyButton = document.getElementById('readyBut')
const falseButton = document.getElementById('falseBut')
const trueButton = document.getElementById('trueBut')
const STAGES = [0,0.1,0.2,1,2,3,4,5,6,7,8,0.3]
const STAGE_1ST_QUESTION = 1


/* ............... STAGES .......................*/ 
var stages = STAGES
stages.curPos = -1
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

/* ...............................................*/ 
var data = []
var answer
var dict,qs;

data["EN"]={"project": "ISISER-SOLNA Project [EN]","title": "Class competition in Electronics","sendingToFurhat": "Sending to Furhat: Question ","instructions": "A few instruccions..","instruction1": "Answer as many questions correctly as fast as possible.","instruction2": "Have a discussion if needed.","instruction3": "You and the robot can change your answer once before confirming.","instruction4": "Both the robot and you must confirm the same answer to be valid.","instruction5": "You can now wake up Furhat...Say hello or something similar!","gettingReady": "Getting ready!","instruction6": "Talking to Furhat until you are both ready","weAreReady": "We are ready","termQuestion": "Question","termConfirm": "Confirm","termTrue": "True ","termFalse": "False ","termNextInstruction": "Next instruction","farewell": "Ok, that was it!","questions": ["qOriginalId|qStatement|qImage","4|In this situation, the red light is off, yellow on, and blue on.|question4_5_6_7.png","15|The three bulbs have the same brightness|question15.png","6|If the switch is connected, the blue light will be brighter than the red and the yellow.|question4_5_6_7.png","17|C1 and C2 are not equivalent, but the blue lights will consume the same power|question17.png","1|All the yellow bulbs have the same brightness|question1_2_3.png","3|In C1, the blue bulb receives more current than in C2|question1_2_3.png","8|If the switch is connected, the yellow light could not be brighter than now|question8.png","9|Despite the difference in resistance between C1 and C2, blue bulbs still consume the same power in both circuits.|question9.png"]}
data["SE"]={"project": "ISISER-SOLNA Project [SE]","title": "Class competition in Electronics","sendingToFurhat": "Sending to Furhat: Question ","instructions": "A few instruccions..","instruction1": "Answer as many questions correctly as fast as possible.","instruction2": "Have a discussion if needed.","instruction3": "You and the robot can change your answer once before confirming.","instruction4": "Both the robot and you must confirm the same answer to be valid.","instruction5": "You can now wake up Furhat...Say hello or something similar!","gettingReady": "Getting ready!","instruction6": "Talking to Furhat until you are both ready","weAreReady": "We are ready","termQuestion": "Question","termConfirm": "Confirm","termTrue": "True ","termFalse": "False ","termNextInstruction": "Next instruction","farewell": "Ok, that was it!","questions": ["qOriginalId|qStatement|qImage","4|In this situation, the red light is off, yellow on, and blue on.|question4_5_6_7.png","15|The three bulbs have the same brightness|question15.png","6|If the switch is connected, the blue light will be brighter than the red and the yellow.|question4_5_6_7.png","17|C1 and C2 are not equivalent, but the blue lights will consume the same power|question17.png","1|All the yellow bulbs have the same brightness|question1_2_3.png","3|In C1, the blue bulb receives more current than in C2|question1_2_3.png","8|If the switch is connected, the yellow light could not be brighter than now|question8.png","9|Despite the difference in resistance between C1 and C2, blue bulbs still consume the same power in both circuits.|question9.png"]}			

// app.js
function requestStage(st){
	var req = {"message": "REQUESTED STAGE [" + (st?st:0) + "]"}
	st = st?st:stages.current
	if(st)req.stage = st
	contactServer(req)
}
function sendAnswer(){
	var req = {"message": "SENDING ANSWER[" + answer + "]",
				"stage": stages.current,
				"answer": answer}
	contactServer(req)
}
function contactServer(req) {
	console.log("Contactig server with req=" + JSON.stringify(req))
	
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
        return response.json();  // Only parse as JSON if the response is OK
    })
    .then(data => {
		console.log("Receiving from server data=" + JSON.stringify(data))
		if(!data || data.status!="0"){
			console.error('[ISISER] Server did not allow the request.')
			return
		}else{
			changeStage(data.stage)
		}
		return data
	})
    .catch(error => {
		console.error('[ISISER] Error:', error)
		return null
	});
}
/*
readyButton.addEventListener('click', function() {
	contactServer("Hellooo", stage)
});
*/
function setAnswer(ans){
	if(!ans){//this means to clear all answers
		answer = null
		falseButton.switchOff()
		trueButton.switchOff()
	}else{
		if(ans != answer){
			if(ans==ANSWER_TRUE){
				trueButton.classList.toggle('active');
				falseButton.switchOff()
			}else{
				falseButton.classList.toggle('active');
				trueButton.switchOff()
			}
			answer = ans
			
		}
	}
}

function tryChangeStage(st){
	console.log("tryChangeStage(st=" + st + ") called")
	st = parseFloat(st)
	st = stages.includes(st)?st:undefined
	if(DEV_MODE){
		changeStage(st)
	}else{//requesting stage to the server
		requestStage(st)
	}
}

function changeStage(st){
	console.log("Changing stage [" + st + "]")
	if(stages.set(st) ){
		st = stages.current
		console.log('slide_'+ (stages.previous<1?stages.previous:"Q"))
		if(stages.previous!=null)document.getElementById('slide_'+ (stages.previous<1?stages.previous:"Q")).style.display = "none"
		if(st >= STAGE_1ST_QUESTION){
			document.getElementById('loadingQnum').innerHTML = st
			document.getElementById('qNum').innerHTML = st
			document.getElementById('qStatement').innerHTML = qs[st].qStatement
			document.getElementById('qImage').src = IMAGES_PATH + qs[st].qImage
			setAnswer()
			window.setTimeout(function(){document.getElementById('slide_'+ (st<1?st:"Q")).style.display = "flex"},1500)
		}else{
			document.getElementById('slide_'+ (st<1?st:"Q")).style.display = "flex"
			if(st==0.2)window.setTimeout(function(){document.getElementById('readyBut').style.visibility="visible"},3000)
		}
	}else{
		console.log("Stage not changed.")
	}
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
function init(){
	if(DEV_MODE)document.getElementById('devmode').style.display="block"
	loadTexts()
	tryChangeStage(URL_VARS.stage)
	
	var isActive = function(){
		return this.classList.contains("active")
	}
	var switchOff = function(){
		if(this.isActive())this.classList.toggle('active')
	}
	falseButton.isActive = isActive
	trueButton.isActive = isActive
	falseButton.switchOff = switchOff
	trueButton.switchOff = switchOff
	
}
/* ----------------------------- INIT ------------------------------ */
init()

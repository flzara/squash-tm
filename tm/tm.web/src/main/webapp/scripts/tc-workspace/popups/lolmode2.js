/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This module exists for the sole purpose of amuse (or piss off) the QA 
 * for the April Fool Day !
 * 
 */

define(["jquery", 'workspace/workspace.delnode-popup', "jqueryui"], 
		function($){
	
	/* a debug function that lessen the level of annoyance
	 * of the dialog, allowing for easier hunt.
	 */ 
	var DEBUG_STILL = false;
	
	// ********* some constants ***************
	
	var NYAN_START_TIME = 0.2;
	var nyannyannyannyan = new Audio('/squash/scripts/tc-workspace/popups/nyan-cat-annoying-music.mp3');
	nyannyannyannyan.loop = true;

	var DEFAULT_WAIT_TIME = 500;

	var WOBBLE_MAX = 50,
		WOBBLE_MIN_DURATION = 500,
		WOBBLE_MAX_DURATION = 1000,

		MOV_MIN_DURATION = 700,
		MOV_MAX_DURATION = 2500,
		
		ROTATE_MIN = -7,
		ROTATE_MAX = 7,
		ROTATE_ENDQUICK = 30,
		
		ESCAPE_TIME = 600,
		BOUNCE_SPEED = 1.0,
		
		RUN_HIDE_TIME = 500,
		HIDE_TIME = 1000,
		GO_PEEK = 2000,
		STAY_PEEK = 0,
		HIDE_AGAIN = 100,
		
		DASH_TIME = 2000,
		
		MANAGER_TIME = 5000;
	 
	var POS_LEFT = 0,
		POS_BOTTOM = 1,
		POS_RIGHT = 2,
		POS_TOP = 3;
	
	
	var DEFAULT_SPAN_STATE = {
		'margin-top' : 75,
		'margin-left' : 5
	};
	
	var WOBBLE_SPAN_UP = {
		'margin-top' : DEFAULT_SPAN_STATE['margin-top'] - WOBBLE_MAX
	};

	var WOBBLE_SPAN_DOWN = {
		'margin-top' : DEFAULT_SPAN_STATE['margin-top'] + WOBBLE_MAX
	};
	
	var MOV_SPAN_LEFT = {
		'margin-left' : 5
	};
	
	var MOV_SPAN_RIGHT = {
		'margin-left' : 295
	};
	
	var ROTATE_0 = {
		'-ms-transform': 'rotate(0deg)',		/* IE 9 */
		'-webkit-transform': 'rotate(0deg)',	/* Chrome, Safari, Opera */
		'transform': 'rotate(0deg)'		
	};


	
	var EASINGS = ["linear", "swing", "easeInQuad", "easeOutQuad", "easeInOutQuad", "easeInCubic", 
		           "easeOutCubic", "easeInOutCubic", "easeInQuart", "easeOutQuart", "easeInOutQuart", 
		           "easeInQuint", "easeOutQuint", "easeInOutQuint", "easeInExpo", "easeOutExpo", 
		           "easeInOutExpo", "easeInSine", "easeOutSine", "easeInOutSine", "easeInCirc", 
		           "easeOutCirc", "easeInOutCirc"];
	
	// *********** utility functions ***********

	function getDimension($object){
		return {
			width  : $object.width(),
			height : $object.height()
		};		
	}
	
	function getPosition(object){
		return {
			left : parseInt(object.css('left').replace('px',''), 10),
			top : parseInt(object.css('top').replace('px',''), 10)
		};
	}
	
	function maxOffsets($dialog){
		var winDim = getDimension($(window)),
			diagDim = getDimension($dialog);
		
		return { 
			maxLeft : (winDim.width - diagDim.width),
			maxTop : (winDim.height - diagDim.height)
		};
	}
	
	
	/*
	 * border : see the variables POS_LEFT, POS_RIGHT etc
	 * 
	 * if undefined : nothing.
	 */
	function randomPosition($dialog, border){

		var max = maxOffsets($dialog);
		
		var pos = {
			top : Math.floor(Math.random() * max.maxTop),
			left : Math.floor(Math.random() * max.maxLeft)
		};
		
		if (border !== undefined){
			switch(border){
				case POS_LEFT : pos.left=0; break;
				case POS_BOTTOM : pos.top=max.maxTop; break;
				case POS_RIGHT : pos.left = max.maxLeft; break;
				default : pos.top = 0; 
			}
		}
		
		return pos;
	}


	
	// *********** effector classes ***********
	
	/*
	 * Simple effectors, that will shake the text a bit
	 */
	
	/*
	 * That object will move a span up and down alternately
	 */
	function SpanWobbler(span){

		var self = this;		
		this.span = span;
		this.duration = null;
		this.queueName = "wobblespanqueue";
		this.state = "up";
		
		this.randomize = function(){
			this.duration = Math.floor(Math.random() * (WOBBLE_MAX_DURATION - WOBBLE_MIN_DURATION)) + WOBBLE_MIN_DURATION;
		};
		

		this.randomize();	
		
		function wobbleUp(){
			var opts = {
				queue : self.queueName,
				duration : self.duration,
				complete : wobbleDown
			};
				
			self.state = "up";
			self.span.animate(WOBBLE_SPAN_UP, opts);
			self.span.dequeue(self.queueName);
		}
		
		function wobbleDown(){
			var opts = {
				queue : self.queueName,
				duration : self.duration,
				complete : wobbleUp
			};
				
			self.state = "down";
			self.span.animate(WOBBLE_SPAN_DOWN, opts);
			self.span.dequeue(self.queueName);
		}
		
		this.start = function(){
			this.stop();
			if (this.state === "up"){
				wobbleUp();
			}
			else{
				wobbleDown();
			}
		};
		
		this.stop = function(){
			this.span.stop(this.queueName, true);
		};
				
		
	}
	
	/*
	 * That object will move a span left and right alternately
	 */
	function SpanMover(span){

		var self = this;

		this.span = span;	
		this.duration = null;
		this.queueName = "movespanqueue";
		
		this.state = "right";
		
		this.randomize = function(){
			this.duration = Math.floor(Math.random() * (MOV_MAX_DURATION - MOV_MIN_DURATION)) + MOV_MIN_DURATION;
		};
		
		this.randomize();	
		
		
		function moveRight(){
			var opts = {
				queue : self.queueName,
				duration : self.duration,
				complete : moveLeft
			};
		
			self.state = "right";
			self.span.animate(MOV_SPAN_RIGHT, opts);
			self.span.dequeue(self.queueName);
		}

		function moveLeft(){
			var opts = {
				queue : self.queueName,
				duration : self.duration,
				complete : moveRight
			};

			self.state = "left";
			self.span.animate(MOV_SPAN_LEFT, opts);
			self.span.dequeue(self.queueName);
		}
		
		this.start = function(){	
			this.stop();
			if (this.state === "right"){
				moveRight();
			}
			else{
				moveLeft();
			}
		};
		
		this.stop = function(){
			this.span.stop(this.queueName, true);
		};
		
	}
	
	
	/* **********************
	 * More complex effectors, that will move the dialog itself.
	 * 
	 * They must define theses functions : 
	 * - start() : init them then start right away
	 * - stop() : stop doing it and clean the envent handlers
	 * 
	 * and also define a state : 'running' or 'complete'
	 *********************** */
	
	/*
	 * This class will rotate the dialog
	 */
	
	function DialogRotator(dialog){
		
		var self = this;
		
		this.dialog = dialog;
		this.incr = 0;
		this.interId = 0;
		this.curAngle = 0;
		
		// those variables are used to, when requested to stop,
		// the rotator still achieves the rotation.
		this.rotationFinished = true;
		this.mustStop = false;
		
		this.randomize = function(){
			this.incr = Math.round(Math.floor(Math.random() * (ROTATE_MAX - ROTATE_MIN)) + ROTATE_MIN);
		};
		
		this.randomize();
		
		
		function rotate(){

			self.curAngle = (self.curAngle + self.incr)%360;
			
			// check if the rotation must stop. We stop it only when finished.
			if (self.mustStop && Math.abs(self.curAngle) <= ROTATE_ENDQUICK){
				self.rotationFinished = true;
				self.curAngle = 0;
			}
			
			var rotation = {
			    '-ms-transform': 'rotate('+self.curAngle+'deg)',	
			    '-webkit-transform': 'rotate('+self.curAngle+'deg)',
			    'transform': 'rotate('+self.curAngle+'deg)'
			};
			
			self.dialog.css(rotation);
			
			if(self.rotationFinished && self.mustStop){
				clearInterval(self.interId);
			}

		}
		
		this.start = function(){
			this.stop();
			if (this.rotationFinished){
				this.rotationFinished=false;
				this.mustStop=false;
				this.interId = setInterval(rotate, 50);
			}
		};
		
		this.stop = function(){
			if (this.rotationFinished){
				clearInterval(this.interId);
				this.rotationFinished=true;
			}
			else{
				this.mustStop = true;
				this.incr = ROTATE_ENDQUICK * Math.sign(this.incr);
			}

		};
				
	}
	
	

	/*
	 * That class will make the dialog flee when the user approaches the mouse.
	 * It will do so for a given number of time. 
	 */
	function DialogEscaper(dialog, howmany){
		
		this.dialog = dialog;
		this.pane = dialog.find(".popup-dialog-buttonpane");
		this.count = 0;
		this.queueName = "dialogmoverqueue";
		this.howmany = howmany;
		
		var self = this;
		var button = this.button;
		
		this.complete = $.Deferred();
		
		
		function slideAway(){

			var dialog = self.dialog;
			
			var newPos = randomPosition(dialog);
			
			var defer = $.Deferred();
			
			dialog.animate(newPos, {
				duration : ESCAPE_TIME,
				easing : "easeOutCubic",
				queue : false,
				complete : defer.resolve
			});
			
			return defer.promise();
			
		}
		
		function furtherSlides(){
			var pane = self.pane;
			pane.off('mouseover', furtherSlides);
			
			slideAway().done(function(){
				self.count++;
				if (self.count < self.howmany){
					pane.on('mouseover', furtherSlides);
				}
				else{
					self.complete.resolve();
				}
			});
		}
		
		this.start = function(){
			this.complete = $.Deferred();
			this.count = 0;
			
			slideAway()
			.done(function(){
				self.pane.on('mouseover', furtherSlides);
			});
			
			return this.complete;
		};
		
		this.stop = function(){
			this.dialog.stop(this.queueName, true);
			this.pane.off('mouseover', furtherSlides);
		};
		
	}
	
	
	/*
	 * This effector will hide and seek the dialog !
	 */
	
	function DialogHideAndSeek(dialog, howmany){
		
		var self = this;
		this.dialog = dialog;
		this.howmany = howmany;
		this.lastBorder = null;
		this.queueName = "dialogmoverqueue";
		
		this.hiddenPosition = null;
		this.peekPosition = null;
		
		this.complete = $.Deferred();
		
		
		function generateHidePosition(){

			var border,
				dialog = self.dialog;
			do{
				border = Math.floor(Math.random() * 4);
			}
			while(border === self.lastBorder);
			self.lastBorder = border;

			var position = randomPosition(dialog, border);
			
			// be sure the dialog is hidden.
			var diagDim = getDimension(dialog);
			
			switch(border){
			case POS_LEFT : position.left -= (diagDim.width+50); break;
			case POS_BOTTOM : position.top += (diagDim.height+50); break;
			case POS_RIGHT : position.left += (diagDim.width+50); break;
			default : position.top -= (diagDim.height+50); break;
			}
			
			self.hiddenPosition = position;
		}
		
		function generatePeekPosition(){
			var peekPosition = $.extend({}, self.hiddenPosition); 
			var dialog = self.dialog;
			var diagDim = getDimension(dialog);
			var screenDim = getDimension($(window));
			
			switch(self.lastBorder){
			case POS_LEFT : peekPosition.left = -(2*diagDim.width/3); break;
			case POS_BOTTOM : peekPosition.top = (screenDim.height - (diagDim.height/3)); break;
			case POS_RIGHT : peekPosition.left = (screenDim.width - (diagDim.width/3)); break;
			default : peekPosition.top = -(2*diagDim.height/3); break; 
			}
			
			self.peekPosition = peekPosition;
		}
		
		this.start = function(){

			this.complete = $.Deferred();
			var dialog = this.dialog;
			
			function step(css, time){
				dialog.animate(css, {
					queue : self.queueName,
					duration : time
				});
			}
			
			for (var i=0; i<this.howmany; i++){
				
				generateHidePosition();
				generatePeekPosition();
				
				// hide				
				/*
				 * For just one hide and seek we want to see the dialog 
				 * read position 1.
				 * 
				 * For further dashes, we want not to see it.
				 */
				var firstDuration = (i === 0) ? RUN_HIDE_TIME : 0;
				step(self.hiddenPosition, firstDuration);
				
				// stay hidden
				step(self.hiddenPosition, HIDE_TIME);
				
				// peek
				step(self.peekPosition, GO_PEEK);
	
				// stay peek
				step(self.peekPosition, STAY_PEEK);
				
				// hide again
				step(self.hiddenPosition, HIDE_AGAIN);
				
				// stay hidden a bit more.
				step(self.hiddenPosition, HIDE_TIME);
				
			}
			
			dialog.queue(self.queueName, self.complete.resolve);
			
			dialog.dequeue(self.queueName);
			
			
			return this.complete.promise();
		};
		
		this.stop = function(){
			this.dialog.stop(this.queueName, true);
			this.complete.reject();
		};
	}
	
	
	/*
	 * This effector will make the dialog dash through the screen
	 */
	function DialogDasher(dialog, howmany){
		
		var self=this;
		
		this.dialog = dialog;
		this.queueName = "dialogmoverqueue";
		this.position = [];
		this.howmany = howmany;
		this.complete = $.Deferred();
		
		function generatePosition(border){

			var position = randomPosition(dialog, border);
			
			// be sure the dialog is hidden.
			var diagDim = getDimension(dialog);
			
			switch(border){
			case POS_LEFT : position.left -= (diagDim.width+400); break;
			case POS_BOTTOM : position.top += (diagDim.height+400); break;
			case POS_RIGHT : position.left += (diagDim.width+400); break;
			default : position.top -= (diagDim.height+400); break;
			}
			
			return position;
		}
		
		function pickBorder(){
			return Math.floor(Math.random() * 4);
		}
		
		function selectOppositeBorder(border){
			return (border+2)%4;
		}
		
		this.start = function(){
			this.complete = $.Deferred();
			
			for (var i=0; i< this.howmany; i++){
				var border = pickBorder();
				var position1 = generatePosition(border);
				
				border = selectOppositeBorder(border);
				var position2 = generatePosition(border);
				
				/*
				 * For just one dash we want to see the dialog 
				 * read position 1.
				 * 
				 * For further dashes, we want not to see it.
				 */
				var firstDuration = (i === 0) ? RUN_HIDE_TIME : 0;
				this.dialog.animate(position1, {
					queue : self.queueName,
					duration : firstDuration 
				});
				
				this.dialog.animate(position1, {
					queue : self.queueName,
					duration : HIDE_TIME 
				});
				
				this.dialog.animate(position2, {
					queue : self.queueName,
					duration : DASH_TIME 
				});
				
				this.dialog.animate(position2, {
					queue : self.queueName,
					duration : HIDE_TIME 
				});
				
			}

			self.dialog.queue(self.queueName, self.complete.resolve );
			dialog.dequeue(this.queueName);
			
			return this.complete.promise();
		};
		
		this.stop = function(){
			this.dialog.stop(this.queueName, true);
			this.complete.reject();
		};
		
	}
	

	/*
	 * This one will make the dialog bounce again and again against the border or the screen
	 */
	function DialogBouncer(dialog, howmany){
		var self = this;
		
		this.dialog = dialog;
		this.howmany = howmany;
		this.lastBorder = Math.floor(Math.random() * 4);
		this.rotation = 1;
		this.queueName = "dialogmoverqueue";
		
		function nextDestination(){
			self.lastBorder = self.lastBorder+self.rotation;
			// I'm angry at modulus
			if (self.lastBorder === -1){
				self.lastBorder = 3;
			}
			else if (self.lastBorder === 4){
				self.lastBorder = 0;
			}
			
			return randomPosition(dialog, self.lastBorder);
			
		}
		
		function computeDuration(pos1, pos2){
			var v = {
				left : pos2.left - pos1.left,
				top : pos2.top - pos1.top
			};
			
			var len = Math.sqrt((v.left*v.left) + (v.top*v.top));
		
			return (len / BOUNCE_SPEED);
			
		}
		
		this.start = function(){

			this.rotation = (Math.random()<0.5) ? -1 : 1;
			
			this.complete = $.Deferred();
			
			this.prevpos = getPosition(dialog);
			
			var	opts = {
				queue : self.queueName
			};
			
			for (var i=0;i<self.howmany; i++){
				//var ease = EASINGS[Math.floor(Math.random() * EASINGS.length)];
				var ease = "linear";
				
				var pos = nextDestination();	
				
				// let's compute which duration we need to maintain the 
				// same speed
				var duration = computeDuration(pos, this.prevpos);
				
				var opt = $.extend({}, opts, { easing : ease, duration : duration});

				self.dialog.animate(pos, opt);
				
				this.prevpos = pos;
			}
			self.dialog.queue(self.queueName, self.complete.resolve );
			
			self.dialog.dequeue(self.queueName);
			
			return self.complete.promise();
		};
		
		this.stop = function(){
			this.dialog.stop(this.queueName, true);
			this.complete.reject();
		};
		
	}
	// *************** effectors manager ****************
	
	function EffectorsManager(dialog, span){
		var self = this;
		
		this.dialog = dialog;
		this.span = span;
		
		this.running = true;
		
		this.spanWobbler = new SpanWobbler(span);
		this.spanMover = new SpanMover(span);
		this.dialogRotator = new DialogRotator(dialog);
		this.dialogEscaper = new DialogEscaper(dialog, 2);
		this.dialogBouncer = new DialogBouncer(dialog, 7);
		this.dialogHider = new DialogHideAndSeek(dialog, 2);
		this.dialogDasher = new DialogDasher(dialog, 2);
		
		this.effector=null;
		
		this.timeId=0;
		
		this.prevMove = "bounce";
		
		function newComplexMove(){
			
			if (self.effector !== null){
				self.effector.stop();
			}

			if (! self.running){
				return;
			}			
			
			var which = ["bounce", "hide", "dash", "escape", "noOOBPlease"];

			var howmany,
				effector;
			
			var move;
			do{
				var idx = Math.floor(Math.random() * which.length);
				move = which[idx];
			}while (move === self.prevMove);

			
			if (DEBUG_STILL){
				// something quiet for CSS debugging
				effector = self.dialogEscaper;
				howmany = Math.floor(Math.random() * 4) + 1;
				self.dialogEscaper.howmany = howmany;
			}
			else{
				switch (move){
				case "bounce" : 
					effector = self.dialogBouncer;
					howmany = Math.floor(Math.random() * 7) + 3;
					self.dialogBouncer.howmany = howmany;
					break;
				case "hide" : 
					effector = self.dialogHider;
					howmany = Math.floor(Math.random() * 2) + 1;
					self.dialogRotator.stop();	// we don't want it to rotate for this one
					self.dialogHider.howmany = howmany;
					break;
				case "dash" : 
					effector = self.dialogDasher;
					howmany = Math.floor(Math.random() * 2) + 1;
					self.dialogDasher.howmany = howmany;
					break;		
				case "escape" : 
					effector = self.dialogEscaper;
					howmany = Math.floor(Math.random() * 4) + 1;
					self.dialogEscaper.howmany = howmany;
					break;						
				default : // default is bouncer
					effector = self.dialogBouncer;
					howmany = Math.floor(Math.random() * 7) + 3;
					self.dialogBouncer.howmany = howmany;
				}
			}

			self.effector = effector;
			self.prevMove = move;
			
			//console.log('next complex move : '+move+" for "+howmany+" times");
			effector.start().done(function(){
				newComplexMove();
			});
		}

		function shouldRotate(){
			 var res = (Math.random()<0.45);
			 return res;
		}
		
		function newBasicMove(){
			
			if (! self.running){
				return;
			}
			
			// the span effectors
			self.spanWobbler.randomize();
			self.spanMover.randomize();
			
			// the rotation
			var activate = (DEBUG_STILL) ? false : shouldRotate();
			if (activate){
				self.dialogRotator.randomize();
				self.dialogRotator.start();
			}
			else{
				self.dialogRotator.stop();
			}

		}
		
		
		this.phase1 = function(){
			self.spanWobbler.start();
			self.spanMover.start();
			
			return self.dialogEscaper.start();			
		};
		
		this.phase2 = function(){
			return self.dialogBouncer.start();
		};
	
		
		this.phase3 = function(){
			window.bouncer = self.dialogBouncer;
			
			newComplexMove();
			
			this.timeId = setInterval(function(){
				newBasicMove();
			}, MANAGER_TIME);
		};

		
		this.start = function(){
			this.running=true;
		};
		
		this.stop = function(){
			this.running=false;
			clearInterval(this.timeId);
			this.spanWobbler.stop();
			this.spanMover.stop();
			this.dialogRotator.stop();
			this.dialogEscaper.stop();
			this.dialogBouncer.stop();
			this.dialogHider.stop();
			this.dialogDasher.stop();
		};
		
		
	}
	
	// *************** the main module ******************
	
	function LolMode(dialog){
		
		var self = this;
		this.dialog = dialog;
		this.runspan = dialog.find('.delete-node-dialog-lol');

		
		this.effectorsManager = new EffectorsManager(this.dialog.parent(), this.runspan);
		
		// ************* management for the state 'nice try' ************
		
		this.nt = {
			timeoutId : undefined,
			endOfFun : false
		};
		
		// ************ annoying music section ************************
		
		this.nyanPlay = function(){
			nyannyannyannyan.play();
		};
		
		this.nyanStopReset = function(){
			nyannyannyannyan.pause();
			nyannyannyannyan.currentTime = NYAN_START_TIME;
		};
		

		// ******** events and basic states *************
		
		function saveState(){
			self.initState = {
				bodyOverflow : $('body').css('overflow'),
				diagLeft : dialog.css('left'),
				diagTop : dialog.css('top')
			};
		}
		
		function restoreState(){
			$('body').css('overflow',self.initState.bodyOverflow);
			dialog.parent().css(ROTATE_0);
			dialog.parent().animate({
				left : self.initState.diagLeft,
				top : self.initState.diagTop
			});
		}
		
		this.init = function(){
			var self = this;
			
			saveState();
			
			// copy the confirm into a copycat
			var confirmHtml = dialog.find('div[data-def="state=confirm"]').html();
			var copyconfirmPane = dialog.find('div[data-def="state=copyconfirm"]');
			copyconfirmPane.html(confirmHtml);

			dialog.delnodeDialog('setState', 'copyconfirm');
			
			$('body').css('overflow', 'hidden');
			
			// reset the annoying music
			self.nyanStopReset();
			
			// init the lol
			self.main();
			
		};
		
		this.clean = function(){
			dialog.off('delnodedialogdeadlyconfirm delnodedialognicetry delnodedialogclose');
	
			
			this.effectorsManager.stop();
			// that one is harder to kill
			this.effectorsManager.dialogRotator.dialog.stop();
			restoreState();
		};
		
		// *********** main loops ********
		
		function initPhase2(){
			dialog.on('delnodedialogdeadlyconfirm', function(){
				self.die();
			});
			
			dialog.on('delnodedialognicetry', function(){
				self.niceTry();
			});
			
			dialog.on('delnodedialogclose', function(){
				self.nyanStopReset();
			});

			// init the lol span
			self.runspan.css(DEFAULT_SPAN_STATE);
			dialog.delnodeDialog('setState', 'lol');	
			
			// let's roll the music too !
			self.nyanPlay();
		}
		
		this.main = function(){
			var manager = this.effectorsManager;
			manager.start();
			
			// phase 1 is just little fun
			manager.phase1().done(function(){
				// now for phase 2 we can giggle a little more 
				// and also enable the confirm button again
				initPhase2();
				
				manager.phase2().done(function(){						
					manager.phase3();						
				});					
			});
		};
		
		// shows the 'nice try' pane, then revert to the lol pane
		this.niceTry = function(){
			var self = this;			
			dialog.delnodeDialog('setState', 'nicetry');
			
			this.nt.timeoutId = setTimeout(function(){
				if (! self.nt.endOfFun){
					dialog.delnodeDialog('setState', 'lol');
				}
			}, 3000);
		};
		
		this.cancelNicetry = function(){
			
			/* 
			 * if lolmode is ending, 
			 * do not change state again if so because it would
			 * interfere with the animation ending (state 'gotcha')
			 */
			this.nt.endOfFun = true;
			if (!!this.nt.timeoutId){
				clearTimeout(this.nt.timeoutId);
			}
		};
		
		function moveToCenter(){
			var scrDim = getDimension($(window));
			var diaDim = getDimension(dialog);
			
			var pos = {
				left : (scrDim.width/2) - (diaDim.width/2),
				top : (scrDim.height/2) - (diaDim.height/2)
			};
			
			dialog.parent().animate(pos);
		}
		
		function hitTheWall(){
			dialog.delnodeDialog('setState', 'gotcha');
			
			// this will reset the gif animation
			var src = $(".delete-node-dialog-gotcha").attr('src');
			$(".delete-node-dialog-gotcha").attr('src', src);
		}
		
		this.die = function(){
			var runspan = this.runspan;

			this.effectorsManager.stop();
			
			this.cancelNicetry();
			moveToCenter();
			hitTheWall();
			this.nyanStopReset();
			
			setTimeout(function(){
				dialog.delnodeDialog('performDeletion');
			}, 3500);
			
		};
		
		// now init
		this.init();
		
	}
	
	return {
		getNew : function(dialog){
			return new LolMode(dialog);
		}
	
		// debug
		, 
		SpanWobbler : SpanWobbler,
		SpanMover : SpanMover,
		defaultState : DEFAULT_SPAN_STATE
	};
	
	
});


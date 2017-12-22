//if (!document.eventsQueue) {
	document.eventsQueue = [];
//}

window.publish = function() {
	document.eventsQueue.push(arguments);
};

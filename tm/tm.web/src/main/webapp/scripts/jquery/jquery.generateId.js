//generates an unique id for various elements
//great thanks to http://blog.amps211.com/this-is-me-professing-my-love-for-jquery-and-how-i-got-ckeditor-working-with-jeditable/

;(function($) {
	
	//no multiple includes please
	if ($.generateId !== undefined) return;
	
	$.generateId = function() {
		//check the id
		
		arguments.callee.count++;
		
		var id = 0;
		for ( instance in CKEDITOR.instances ){
			//get the count value, after "jq$"
			id = instance.substr(3);
		}
		//Check if there's an CKEDITOR instance with the same id
		if(arguments.callee.count <= id){
			arguments.callee.count = ++id;
		}		
		
		return arguments.callee.prefix + arguments.callee.count;
	};
	$.generateId.prefix = 'jq$';
	$.generateId.count = 0;
	
	$.fn.generateId = function() {
		return this.each(function() {
			this.id = $.generateId();
		});
	};
})(jQuery);
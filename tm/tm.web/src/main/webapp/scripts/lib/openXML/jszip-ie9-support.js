
define([], function(){
	
	(function checkTypedArrayCompatibility() {
		  if (typeof Uint8Array !== 'undefined') {
		    // some mobile versions do not support subarray (e.g. safari 5 / iOS)
		    if (typeof Uint8Array.prototype.subarray === 'undefined') {
		        Uint8Array.prototype.subarray = function subarray(start, end) {
		          return new Uint8Array(this.slice(start, end));
		        };
		        Float32Array.prototype.subarray = function subarray(start, end) {
		          return new Float32Array(this.slice(start, end));
		        };
		    }

		    // some mobile version might not support Float64Array
		    if (typeof Float64Array === 'undefined'){
		      window.Float64Array = Float32Array;
		    }
		    return;
		  }

		  function subarray(start, end) {
		    return new TypedArray(this.slice(start, end));
		  }

		  function setArrayOffset(array, offset) {
		    if (arguments.length < 2){
		      offset = 0;}
		    for (var i = 0, n = array.length; i < n; ++i, ++offset){
		      this[offset] = array[i] & 0xFF;}
		  }

		  function TypedArray(arg1) {
		    var result;
		    if (typeof arg1 === 'number') {
		      result = [];
		      for (var i = 0; i < arg1; ++i){
		        result[i] = 0;}
		    } else{
		      result = arg1.slice(0);}

		    result.subarray = subarray;
		    result.buffer = result;
		    result.byteLength = result.length;
		    result.set = setArrayOffset;

		    if (typeof arg1 === 'object' && arg1.buffer)
		    { result.buffer = arg1.buffer;}

		    return result;
		  }

		  window.Uint8Array = TypedArray;

		  // we don't need support for set, byteLength for 32-bit array
		  // so we can use the TypedArray as well
		  window.Uint32Array = TypedArray;
		  window.Int32Array = TypedArray;
		  window.Uint16Array = TypedArray;
		  window.Float32Array = TypedArray;
		  window.Float64Array = TypedArray;
	})();	
	
});
// copy of worker.js with change for fake implementation. Remove this class and use worker.js when we stop supporting IE9
define(function () {
	return {
		version: "1.0.1",
		load: function (name, req, onLoad, config) {
			if (config.isBuild) {
				//don't do anything if this is a build, can't inline a web worker
				onLoad();
				return;
			}

			var url = req.toUrl(name);

			if (window.Worker) {
				onLoad(new Worker(url));
			} else {
			//no fake implementation, cuz fake implementation currently doesn't support import script. 
		
					onLoad();
			}
		}
	};
});
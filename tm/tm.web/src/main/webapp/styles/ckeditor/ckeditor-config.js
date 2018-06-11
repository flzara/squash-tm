CKEDITOR.editorConfig = function( config ){
	config.skin = 'moonocolor';
	config.toolbar = 'Squash';
	config.toolbar_Squash =
		[
		['Bold','Italic','Underline','NumberedList','BulletedList'],
		['Link'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		['TextColor'],['Font'],['FontSize'],
		['Scayt'],
		['Table', 'Image'],
		['Maximize']
		];
	config.height = '10em';
	config.resize_minHeight = 	175;
	config.resize_minWidth = 200;
	config.removePlugins = 'elementspath';
	config.extraPlugins='onchange';
	config.scayt_autoStartup = true;
	config.scayt_sLang = getLocale();
	config.scayt_disableOptionsStorage = 'lang';
}

getLocale = function(){
	var locale = squashtm.app.locale;
	switch(locale) {
		case 'fr': return 'fr_FR';
		case 'de': return 'de_DE';
		case 'es': return 'es_ES';
		case 'uk': return 'en_GB';
		default : return 'en_US';
	}
}

CKEDITOR.editorConfig = function( config ){
	config.skin = 'moonocolor';
	config.toolbar = 'Squash';
	config.toolbar_Squash =
		[
		['Bold','Italic','Underline','Strike','NumberedList','BulletedList'],
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
	initScayt(config);
}

initScayt = function(config){
	var locale = getLocale();
	switch(locale) {
		case 'fr': enableScayt(config, 'fr_FR'); break;
		case 'de': enableScayt(config, 'de_DE'); break;
		case 'es': enableScayt(config, 'es_ES'); break;
		case 'en-gb': enableScayt(config, 'en_GB'); break;
		case 'en': enableScayt(config, 'en_US'); break;
		case 'it': enableScayt(config, 'it_IT'); break;
		case 'da': enableScayt(config, 'da_DK'); break;
		case 'nl': enableScayt(config, 'nl_NL'); break;
		case 'no': enableScayt(config, 'nb_NO'); break;
		case 'sv': enableScayt(config, 'sv_SE'); break;
		case 'el': enableScayt(config, 'el_GR'); break;
		case 'en-ca': enableScayt(config, 'en_CA'); break;
		case 'fr-ca': enableScayt(config, 'fr_CA'); break;
		case 'fi': enableScayt(config, 'fi_FI'); break;
		default : disableScayt(config); break;
	}
}

enableScayt = function(config, locale) {
		config.scayt_autoStartup = true;
  	config.scayt_disableOptionsStorage = 'lang';
  	config.scayt_sLang = locale;
}

disableScayt = function(config) {
		config.scayt_autoStartup = false;
}

getLocale = function() {
		var locale = navigator.language || navigator.userLanguage;

  	if (locale === 'en-GB')	return 'en-gb';
  	if (locale === 'en-CA') return 'en-ca';
  	if (locale === 'fr-CA') return 'fr-ca';

		return locale.length > 2 ? locale.substring(0, 2) : locale;
}


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
}

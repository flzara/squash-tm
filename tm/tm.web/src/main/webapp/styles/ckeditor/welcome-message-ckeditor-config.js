CKEDITOR.editorConfig = function( config ){
	config.skin = 'moonocolor';
	config.toolbar = 'Welcome';
	config.toolbar_Welcome =  
		[   
		[ 'Source','-','Save','NewPage','DocProps','Preview','Print','-','Templates' ],
		[ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ],
		[ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ],
		[ 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField' ],
		
		[ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ],
		[ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ],
		[ 'Link','Unlink','Anchor' ],
		[ 'Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak','Iframe' ],

		[ 'Styles','Format','Font','FontSize' ],
		[ 'TextColor','BGColor' ],
		[ 'Maximize', 'ShowBlocks','-','About' ],
		]; 
	config.resize_dir = 'vertical';

	config.removePlugins = 'elementspath';
}

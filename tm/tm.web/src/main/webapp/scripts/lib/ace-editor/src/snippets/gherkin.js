define("ace/snippets/gherkin", ["require", "exports", "module"], function (require, exports, module) {
	"use strict";
	// @formatter:off
	// DO NOT REFORMAT THE SNIPPETS, INDENTATION MUST BE PRESERVED
	exports.snippetText =
"snippet sc\n\
	\n\
	Scenario: ${1}\n\
		Given ${2}\n\
		When ${3}\n\
snippet scout\n\
	\n\
	Scenario Outline: ${1}\n\
		Given ${2}\n\
		When ${3}\n\
		Then ${4}\n\
		Examples: ${5}\n\
snippet tab3*2\n\
	| ${1}\t| ${2}\t| ${3}\t|\n\
	| ${4}\t| ${5}\t| ${6}\t|\n\
snippet tab4*2\n\
	| ${1}\t| ${2}\t| ${3}\t| ${4}\t|\n\
	| ${5}\t| ${6}\t| ${7}\t| ${8}\t|\n\
snippet tab3*3\n\
	| ${1}\t| ${2}\t| ${3}\t|\n\
	| ${4}\t| ${5}\t| ${6}\t|\n\
	| ${7}\t| ${8}\t| ${9}\t|\n\
snippet tab4*3\n\
	| ${1}\t| ${2}\t| ${3}\t| ${4}\t\n\
	| ${5}\t| ${6}\t| ${7}\t| ${8}\t\n\
	| ${9}\t| ${10}\t| ${11}\t| ${12}\t\n\
";
	// @formatter:on
	console.log(exports.snippetText);
	exports.scope = "gherkin";

});

define("ace/snippets/gherkin", ["require", "exports", "module"], function (require, exports, module) {
	"use strict";
	// @formatter:off
	// DO NOT REFORMAT THE SNIPPETS, INDENTATION MUST BE PRESERVED
	exports.snippetText =
"snippet sc\n\
	\n\
	Scenario: ${1}\n\n\
		Given ${2}\n\n\
		When ${3}\n\n\
snippet scout\n\
	\n\
	Scenario Outline: ${1}\n\n\
		Given ${2}\n\n\
		When ${3}\n\n\
		Then ${4}\n\n\
		Examples: ${5}\n\n\
snippet tab3*2\n\
	| ${1}\t\t\t\t| ${2}\t\t\t\t| ${3}\t\t\t\t|\n\
	| ${4}\t\t\t\t| ${5}\t\t\t\t| ${6}\t\t\t\t|\n\
snippet tab4*2\n\
	| ${1}\t\t\t\t| ${2}\t\t\t\t| ${3}\t\t\t\t| ${4}\t\t\t\t|\n\
	| ${5}\t\t\t\t| ${6}\t\t\t\t| ${7}\t\t\t\t| ${8}\t\t\t\t|\n\
snippet tab3*3\n\
	| ${1}\t\t\t\t| ${2}\t\t\t\t| ${3}\t\t\t\t|\n\
	| ${4}\t\t\t\t| ${5}\t\t\t\t| ${6}\t\t\t\t|\n\
	| ${7}\t\t\t\t| ${8}\t\t\t\t| ${9}\t\t\t\t|\n\
snippet tab4*3\n\
	| ${1}\t\t\t\t| ${2}\t\t\t\t| ${3}\t\t\t\t| ${4}\t\t\t\t\n\
	| ${5}\t\t\t\t| ${6}\t\t\t\t| ${7}\t\t\t\t| ${8}\t\t\t\t\n\
	| ${9}\t\t\t\t| ${10}\t\t\t\t| ${11}\t\t\t\t| ${12}\t\t\t\t\n\
";
	// @formatter:on
	console.log(exports.snippetText);
	exports.scope = "gherkin";

});

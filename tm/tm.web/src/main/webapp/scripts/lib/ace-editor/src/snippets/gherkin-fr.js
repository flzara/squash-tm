define("ace/snippets/gherkin-fr", ["require", "exports", "module"], function (require, exports, module) {
	"use strict";
	// @formatter:off
	// DO NOT REFORMAT THE SNIPPETS, INDENTATION MUST BE PRESERVED
	exports.snippetText =
"\
snippet scenario\n\
  \n\
	Scénario: ${1}\n\
		Soit ${2}\n\
		Quand ${3}\n\
		Alors ${4}\n\
snippet param\n\
	<${1}>\n\
snippet plan_scenario\n\
  \n\
	Plan du scénario: ${1}\n\
		Soit ${2}\n\
		Quand ${3}\n\
		Alors ${4}\n\
		Exemples: ${5}\n\
snippet tab3*2\n\
	| ${1}\t| ${2}\t| ${3}\t|\n\
	| ${4}\t| ${5}\t| ${6}\t|\n\
	| ${7}\t| ${8}\t| ${9}\t|\n\
snippet tab4*2\n\
	| ${1}\t| ${2}\t| ${3}\t| ${4}\t|\n\
	| ${5}\t| ${6}\t| ${7}\t| ${8}\t|\n\
	| ${9}\t| ${10}\t| ${11}\t| ${12}\t|\n\
snippet tab3*3\n\
	| ${1}\t| ${2}\t| ${3}\t|\n\
	| ${4}\t| ${5}\t| ${6}\t|\n\
	| ${7}\t| ${8}\t| ${9}\t|\n\
	| ${10}\t| ${11}\t| ${12}\t|\n\
snippet tab4*3\n\
	| ${1}\t| ${2}\t| ${3}\t| ${4}\t\n\
	| ${5}\t| ${6}\t| ${7}\t| ${8}\t\n\
	| ${9}\t| ${10}\t| ${11}\t| ${12}\t\n\
	| ${13}\t| ${14}\t| ${15}\t| ${16}\t\n\
";
// @formatter:on
	console.log(exports.snippetText);
	exports.scope = "gherkin-fr";

});

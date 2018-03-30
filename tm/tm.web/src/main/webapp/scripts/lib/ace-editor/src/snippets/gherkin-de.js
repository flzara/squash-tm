define("ace/snippets/gherkin-fr", ["require", "exports", "module"], function (require, exports, module) {
	"use strict";
	// @formatter:off
	// DO NOT REFORMAT THE SNIPPETS, INDENTATION MUST BE PRESERVED
	exports.snippetText =
"snippet scenario\n\
  \n\
  Scénario: ${1}\n\
		Soit ${2}\n\n\
		Quand ${3}\n\n\
		Alors ${4}\n\
snippet plan_scenario\n\
  \n\
	Plan du scénario: ${1}\n\n\
		Soit ${2}\n\n\
		Quand ${3}\n\n\
		Alors ${4}\n\n\
		Exemples: ${5}\n\
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
	exports.scope = "gherkin-fr";

});

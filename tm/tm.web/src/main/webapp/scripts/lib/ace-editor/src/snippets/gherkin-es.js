define("ace/snippets/gherkin-es", ["require", "exports", "module"], function (require, exports, module) {
	"use strict";
	// @formatter:off
	// DO NOT REFORMAT THE SNIPPETS, INDENTATION MUST BE PRESERVED
	exports.snippetText =
"snippet escenario\n\
  \n\
  Escenario: ${1}\n\
		Dado ${2}\n\n\
		Cuando ${3}\n\n\
		Entonces ${4}\n\
snippet esquema_del_escenario\n\
  \n\
	Esquema del escenario: ${1}\n\n\
		Dado ${2}\n\n\
		Cuando ${3}\n\n\
		Entonces ${4}\n\n\
		Ejemplos: ${5}\n\
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
	exports.scope = "gherkin-es";

});

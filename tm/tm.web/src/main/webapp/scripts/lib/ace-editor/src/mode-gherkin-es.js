define("ace/mode/gherkin_highlight_rules_es",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;
var stringEscape =  "\\\\(x[0-9A-Fa-f]{2}|[0-7]{3}|[\\\\abfnrtv'\"]|U[0-9A-Fa-f]{8}|u[0-9A-Fa-f]{4})";

var GherkinHighlightRules = function() {

	var keywords = [
		"Dado",
		"Dada",
		"Dados",
		"Dadas",
		"Y",
		"E",
		"Pero",
		"Cuando",
		"Entonces"
		].join("|");


    var languages = [{
        name: "es",
        labels: "Característica|Antecedentes|Escenario|Esquema del escenario|Ejemplos",
        keywords: keywords
    }];

    var labels = languages.map(function(l) {
        return l.labels;
    }).join("|");
    var keywords = languages.map(function(l) {
        return l.keywords;
    }).join("|");
    this.$rules = {
        start : [{
            token: "constant.numeric",
            regex: "(?:(?:[1-9]\\d*)|(?:0))"
        }, {
            token : "comment",
            regex : "#.*$"
        }, {
            token : "keyword",
            regex : "(?:" + labels + "):|(?:" + keywords + ")\\b"
        }, {
            token : "keyword",
            regex : "\\*"
        }, {
            token : "string",           // multi line """ string start
            regex : '"{3}',
            next : "qqstring3"
        }, {
            token : "string",           // " string
            regex : '"',
            next : "qqstring"
        }, {
            token : "text",
            regex : "^\\s*(?=@[\\w])",
            next : [{
                token : "text",
                regex : "\\s+"
            }, {
                token : "variable.parameter",
                regex : "@[\\w]+"
            }, {
                token : "empty",
                regex : "",
                next : "start"
            }]
        }, {
            token : "comment",
            regex : "<[^>]+>"
        }, {
            token : "comment",
            regex : "\\|(?=.)",
            next : "table-item"
        }, {
            token : "comment",
            regex : "\\|$",
            next : "start"
        }],
        "qqstring3" : [ {
            token : "constant.language.escape",
            regex : stringEscape
        }, {
            token : "string", // multi line """ string end
            regex : '"{3}',
            next : "start"
        }, {
            defaultToken : "string"
        }],
        "qqstring" : [{
            token : "constant.language.escape",
            regex : stringEscape
        }, {
            token : "string",
            regex : "\\\\$",
            next  : "qqstring"
        }, {
            token : "string",
            regex : '"|$',
            next  : "start"
        }, {
            defaultToken: "string"
        }],
        "table-item" : [{
            token : "comment",
            regex : /$/,
            next : "start"
        }, {
            token : "comment",
            regex : /\|/
        }, {
            token : "string",
            regex : /\\./
        }, {
            defaultToken : "string"
        }]
    };
    this.normalizeRules();
};

oop.inherits(GherkinHighlightRules, TextHighlightRules);

exports.GherkinHighlightRules = GherkinHighlightRules;
});

define("ace/mode/gherkin-es",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/gherkin_highlight_rules_es"], function(require, exports, module) {

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var GherkinHighlightRules = require("./gherkin_highlight_rules_es").GherkinHighlightRules;

var Mode = function() {
    this.HighlightRules = GherkinHighlightRules;
    this.$behaviour = this.$defaultBehaviour;
};
oop.inherits(Mode, TextMode);

(function() {
    this.lineCommentStart = "#";
    this.$id = "ace/mode/gherkin-es";

    this.getNextLineIndent = function(state, line, tab) {
        var indent = this.$getIndent(line);
        var space2 = "  ";

        var tokenizedLine = this.getTokenizer().getLineTokens(line, state);
        var tokens = tokenizedLine.tokens;

        console.log(state);

        if(line.match("[ ]*\\|")) {
            indent += "| ";
        }

        if (tokens.length && tokens[tokens.length-1].type == "comment") {
            return indent;
        }

			var keywords = [
				"Dado",
				"Dada",
				"Dados",
				"Dadas",
				"Entonces"
			].join("|");


        if (state == "start") {
            if (line.match("Escenario:|Característica:|Esquema del escenario:|Antecedentes:")) {
                indent += space2;
            } else if(line.match("("+ keywords +").+(:)$|Ejemplos:")) {
                indent += space2;
            } else if(line.match("\\*.+")) {
                indent += "* ";
            }
        }


        return indent;
    };
}).call(Mode.prototype);

exports.Mode = Mode;
});

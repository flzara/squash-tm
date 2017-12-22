Squash TM web module
====================

This module contains the Squash TM webapp layer

Maven profiles
--------------

* default : this profile will perform compilation for java resources, optimization of web resources (js, spritemaps, less).
It also runs both java unit tests and js unit tests.

* `eclipse` : automatically activated when folder is an eclipse project. Copies files so that the project correctly runs under eclipse 
and configures the webapp so that it does not cache web resources / templates 

* `reports` : runs jshint


Maven properties
----------------

One can tweak the build using these properties : 

* `jasmine.webdriver.classname` : defaults to HtmlUnitDriver. One can use any other available [WebDriver][wd] 
by overriding this property in `settings.xml`

* `requirejs.optimize.params` : can be used to ask r.js optimizer not to optimize a given file. Usage :

    
    mvn compile -Drequirejs.optimize.params="excludeShallow=<module logical name>"
    
    
* `requirejs.optimize.skip` : skips r.js optimization when this 

* `webopt.skip` : skips wro4j css optimization

* `npm.skip` : skips node installation and `npm install` to speed up build. Can usually be set to `true` on dev platforms

[wd]: http://searls.github.io/jasmine-maven-plugin/bdd-mojo.html#webDriverClassName

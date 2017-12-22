Javascript libraries guidelines
===============================

This document explains how to name and where to put javascript libraries

Directory layout
----------------
All third party libs should be in `scripts/lib` or in subfolders. **There should not be new files in `scripts/jquery`**.

When adding third party libs, add both minified and plain files. Also add library version number in the filename if missing.

Top level controller for a page named `page-name.html` should preferrably be in `scripts/page-name.js` or in `scripts/page-name/main.js`. Other files should be stored in a folder with sensible name.
For example, app wide workspace related handlers go in `scripts/workspace`. **Please do not add new files in `scripts/app`, `scripts/squash` or `scripts/squashtest`

commons.js (shimmed libraries)
------------------------------
** Any new js file should be AMD / require.js compliant **
Yet, when adding a library which is not AMD compliant, it should be declared in `commons.js`. 

In the `path` section, it should be mapped to a sensible **lowercase, non hyphenated** name :

* A third-party library, such as jquery or backbone, maps to "libname"
* A third-party library _plugin_, such as jquery cookie plugin to "libname.pluginname"
* A squash jquery plugin / widget or a heavily jquery based module should map to "jquery.squash.pluginname"  
* A squash _library_ should map to "squash.libname" 

**There is no need to map the path of AMD modules**, use a sensible path / name head-on.

In the `shim` section, dependencies of the library should be declared.
    "jqueryui" : { // MAPPED name of the lib
        deps : [ "jquery" ] // MAPPED names of dependencies
    }
When the library exposes itself through a global variable, you should declare it. Otherwise, require will not be able to inject the module.
For example, Underscore exposes itself through the `_` global variable. It is declared this way
    "underscore" : {
        exports : "_"
    }

**Non AMD modules will not be injectable when not declared in the `shim` section**
** AMD modules should not be declared in the `shim`, their dependencies and export are already expressed in the module !**

Backbone classes
----------------
Put them in a sensibly-named directory. Follow these naming guidelines :

* Camelcase 
* Views : Append the name of the "widget" which is managed by the View eg. NewTestCaseDialog.js, UserPermissionPanel.js. If nothing comes to mind, simply append View, eg. ReportWorkspaceView.js
* Model : Append Model... Also consider using a simple Backbone.Model when you do not need to specialize the class.
* Collection : Append Collection... Also consider using a simple Backbone.Collection when you do not need to specialize the class.
* Router : Append Router

Jquery / jqueryui plugins
-------------------------
Follow these naming guidelines when naming files (more or less jquery rules) :

* non-hyphenated lowercase
* `jquery.squash.pluginname.js` for squash specific plugins, eg. confirmdialog.
* `jquery.pluginname.js` if it's not directly squash-related (no example comes to mind)


Backbone / jQuery / Underscore usage guidelines
===============================================

Sometimes, you have the choice between several helper methods / idioms. Please comply to these guidelines

use strict
----------

When creating / modifying a js file, add a "use strict" directive. It will enforce correct js syntax both at runtime and when linted : 
    
    require(..., function() {
      "use strict"; // should be INSIDE function, not outside
      
      // your code below
    })

jQuery is for DOM / Ajax
------------------------
Do not use jquery for non-dom operations such as $.proxy or $.each. There are equivalents in underscore.js. In particular, collection manipulation is more powerful and more regular in underscore.
 
As a rule of thumb, do not automatically require jquery

Setting the context of a Backbone handler
-----------------------------------------
When you need to set the context of a method (usually a backbone handler) do not write this (pseudo-code): 

    Backbone.View.extend({
      _someHandler: function(event) {
        this.doStuff()
      }
      _someOtherHandler: function(event) {
        this.doStuff()
      }
      initialize: function() {
        this.someHandler = $.proxy(this._someHandler, this)
        this.someOtherHandler = $.proxy(this._someOtherHandler, this)
      }
    })

Do not write a useless, confusing "_someHandler" method, remove the starting "_" and redefine the function. You can redefine all functions in a single statement using `_.bindAll()` 

    Backbone.View.extend({
      someHandler: function(event) {
        this.doStuff()
      }
      initialize: function() {
        this.someHandler = _.bindAll(this, "someHandler", "someOtherHandler")
      }
    })


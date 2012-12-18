This is a minimal Maven project implementing an ImageJ 1.x plugin in Clojure

It is intended as an ideal starting point for Clojure fans, to demonstrate
how to develop new ImageJ 1.x plugins in their favorite excuse of a programming
language.

After building the project, you will have the .jar file in the _target/_
subdirectory. Simply copy it into your ImageJ's _plugins/_ directory, run
_Help&gt;Refresh Menus_ and the plugin will be available in _Process&gt;Process
Pixels_ (this can be changed by editing _src/main/resources/plugins.config_).

Since this project is intended as a starting point for your own
developments, it is in the public domain.

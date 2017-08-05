Use case 1: developer build (in your local Jython copy)
-------------------------------------------------------
- call target 'developer-build' (the default for this build.xml)

This build will create directories /build and /dist below basedir.


Use case 2: full build for a release (using hg checkout)
---------------------------------------------------------
- make sure you have access to the Jython mercurial repository
(http://hg.python.org/jython)
- override ant.properties (if necessary)
- call target 'full-build'

This build will create a working directory named
full_build at the same level as your local directories
jython and installer.  It will contain a big
jython_installer-${jython.version}.jar file suitable for installation.

To build older releases, it may be necessary to use an older
build.xml, too (with the corresponding tag).  For example it is not
possible to build Release_2_2alpha1 with this version of build.xml.

Note on targets
---------------
A subset of the available targets are designed for direct invocation.
Following an ant convention, the callable targets have a description
attribute.  Use ant -p to display these targets.  All other targets
may behave unpredictably if called directly.


Where ant looks for ant.properties 
----------------------------------
1. in user.home
2. in the same directory as this build.xml file
The first setting of a property wins. Further settings are ignored.


Actions for a release
---------------------
See http://wiki.python.org/jython/JythonDeveloperGuide/HowToReleaseJython


An example ant.properties file:
-------------------------------

# - zxJDBC
oracle.jar=C:/workspace/HEAD/for_development/bisdevsrv28/jboss/server/infra/lib/ojdbc14.jar
#informix.jar=${basedir}/../externals/external-jars/ifxjdbc.jar

# - option for javac (build.compiler=modern is a global option to use standard jdk 1.7/1.8)
#build.compiler=modern
#jdk.target.version=1.8
#debug=false
#deprecation=off


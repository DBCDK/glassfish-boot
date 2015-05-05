# glassfish-boot

## Building it:

``./gradlew clean build``

## Running it:

``java -jar builds/libs/glassfish-boot-<version>.jar --max-threads=500 --min-threads=300 --app=some-application.ear --mapped-app=/context/root:another-application.war --command-file=my-asadmin-multimode-commands.txt``

* ``--min-threads`` defaults to max-threads/2
* ``--app`` is repeatable
* ``--mapped-app`` is repeatable
* ``--command-file`` is repeatable
* the $cwd must contain an extracted glassfish in a folder named ``glassfish4``
* all ``.jar`` files found in $cwd will be copied to lib/ext of the domain

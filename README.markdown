To write a test:
 Create a directory under `tests/`.  Add a script called `test` and source files for compilation.  For now, see existing tests for examples.

To run tests, use [sbt 0.6.x](http://simple-build-tool.googlecode.com/files/xsbt-launch-0.6.10.jar):

{{{
    $ sbt
    > update
    > run <path to $SCALA_HOME/build/pack/> tests/<test name>/test
}}}

 * `update` only needs to be run the first time.
 * Only one test can be run at a time currently- this should be easy to modify.
 *  The error messages are poor when the arguments to `run` are omitted or not well-formed.
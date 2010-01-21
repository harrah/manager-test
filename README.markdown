To write a test:
 Create a directory under `tests/`.  Add a script called `test` and source files for compilation.  For now, see existing tests for examples.

To run tests, use [sbt 0.6.x](http://simple-build-tool.googlecode.com/files/xsbt-launch-0.6.10.jar):

    $ sbt
    > update
    > run <path to $SCALA_HOME/build/pack/> tests/<test name>/test
    > run <path to $SCALA_HOME/build/pack/> tests/*/test

 * `update` only needs to be run the first time.
 * The first `run` runs a specific test.  The second invocation uses a wildcard to run all tests.
 
 
 Test case notes:
 * The `specialized` test does not work.  I'm not sure how to test that a method call properly uses a specialized method.
 * `sealed` requires manual verification.  The test passes if a warning about exhaustiveness is printed.
 * `thrash` requires manual verification.  The test passes if each source file is only recompiled a couple times and not 15 times each.
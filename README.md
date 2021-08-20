deviation-LearnLib-0.14.0-vs-0.16.0
===================================

This repository contains the code to reproduce the deviation in LearnLib 0.14.0 and 0.16.0 in LearnLib's BBC functionality.

What happens
------------

In LearnLib 0.16.0, the report of counterexample due to model checking does not work well.

How to reproduce
----------------

### LearnLib 0.14.0

```sh
    cd 0.14.0
    mvn package
    java -jar target/deviation-LearnLib-0.14.0-vs-0.16.0-1.0-SNAPSHOT-jar-with-dependencies.jar 
```

### LearnLib 0.16.0

```sh
    cd 0.16.0
    mvn package
    java -jar target/deviation-LearnLib-0.14.0-vs-0.16.0-1.0-SNAPSHOT-jar-with-dependencies.jar 
```

Reason of the deviation
-----------------------

It seems this deviation is due to the modification of [AbstractLTSminMonitorMealy.java](https://github.com/LearnLib/automatalib/blob/1e9e9fa17a7d704808027fd1be9b3ae357f1fb65/modelchecking/ltsmin/src/main/java/net/automatalib/modelcheckers/ltsmin/monitor/AbstractLTSminMonitorMealy.java#L146) in [this](https://github.com/LearnLib/automatalib/commit/75b1987782c8f0167534a5e0b60ec285a11ab6f9) commit (not in LearnLib but automatalib). This changed the result of computeStateOutput from `null` to `Word.epsilon()` when the reached state is not equal to `deadlock`. However, in [`AutomatonOracle::MealyOracle::accepts`](https://github.com/LearnLib/learnlib/blob/02d9d401b1af85ed80447e92f09640880ed4e398/api/src/main/java/de/learnlib/api/oracle/AutomatonOracle.java#L200), the comparison is still against `null`. This changed the behavior of `findCounterExample`.

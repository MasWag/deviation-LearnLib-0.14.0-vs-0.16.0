package io.github.MasWag;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LoggingPropertyOracle;
import de.learnlib.api.oracle.EmptinessOracle;
import de.learnlib.api.oracle.InclusionOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.oracle.emptiness.MealyBFEmptinessOracle;
import de.learnlib.oracle.equivalence.*;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.property.MealyFinitePropertyOracle;
import de.learnlib.util.Experiment;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.modelcheckers.ltsmin.monitor.LTSminMonitorIOBuilder;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ArrayAlphabet;

import java.util.Objects;
import java.util.function.Function;

public class Main14 {
    private final Alphabet<String> inputAlphabet = new ArrayAlphabet<>("a");
    private ModelChecker.MealyModelChecker<String, String, String, MealyMachine<?, String, ?, String>> modelChecker;
    String property = "X (output == \"2\")";
    private MealyMachine<?, String, ?, String> learnedMealy;
    private SUL<String, String> sul;
    private static final Function<String, String> EDGE_PARSER = s -> s;
    private LearningAlgorithm.MealyLearner<String, String> learner;
    private EQOracleChain.MealyEQOracleChain<String, String> eqOracle;
    final private double multiplier = 1.0;

    public Main14() {
        CompactMealy<String, String> mealy = new CompactMealy<>(inputAlphabet);
        mealy = AutomatonBuilders.forMealy(mealy).
                withInitial("q0").
                from("q0").
                on("a").withOutput("1").to("q1").
                from("q1").
                on("a").withOutput("2").to("q0").
                create();
        this.sul = new MealySimulatorSUL<>(mealy);
        MembershipOracle.MealyMembershipOracle<String, String> memOracle = new SULOracle<>(sul);

        this.learner = new TTTLearnerMealy<>(this.inputAlphabet, memOracle, AcexAnalyzers.LINEAR_FWD);

        // Create model checker
        modelChecker = new LTSminMonitorIOBuilder<String, String>()
                .withString2Input(EDGE_PARSER).withString2Output(EDGE_PARSER).create();

        // create an emptiness oracle, that is used to disprove properties
        EmptinessOracle.MealyEmptinessOracle<String, String>
                emptinessOracle = new MealyBFEmptinessOracle<>(memOracle, multiplier);

        // create an inclusion oracle, that is used to find counterexamples to hypotheses
        InclusionOracle.MealyInclusionOracle<String, String>
                inclusionOracle = new MealyBFInclusionOracle<>(memOracle, multiplier);

        eqOracle = new EQOracleChain.MealyEQOracleChain<>(new CExFirstOracle.MealyCExFirstOracle<>(
                new LoggingPropertyOracle.MealyLoggingPropertyOracle<>(
                                new MealyFinitePropertyOracle<>(property, inclusionOracle, emptinessOracle,
                                        modelChecker))));
    }

    public static void main(String[] args){
        System.out.println(new Main14().run());
    }

    boolean run() {
        // create an experiment
        Experiment.MealyExperiment<String, String>
                experiment = new Experiment.MealyExperiment<>(learner, eqOracle, this.inputAlphabet);
        this.learnedMealy = experiment.run();
        Visualization.visualize(learnedMealy, this.inputAlphabet);
        return Objects.isNull(modelChecker.findCounterExample(learnedMealy, this.inputAlphabet, property));
    }
}

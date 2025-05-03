package runners.msrcpsp;

import algorithms.evaluation.BaseEvaluator;
import algorithms.evaluation.BaseScheduleEvaluator;
import algorithms.evaluation.EvaluatorType;
import algorithms.evolutionary_algorithms.ParameterSet;
import algorithms.evolutionary_algorithms.converters.ConverterType;
import algorithms.evolutionary_algorithms.crossover.CrossoverType;
import algorithms.evolutionary_algorithms.genetic_algorithm.CRSGA_MSRCPSP;
import algorithms.evolutionary_algorithms.genetic_algorithm.utils.OptimisationResult;
import algorithms.evolutionary_algorithms.initial_population.*;
import algorithms.evolutionary_algorithms.mutation.MutationType;
import algorithms.evolutionary_algorithms.selection.IndividualsPairingMethod;
import algorithms.evolutionary_algorithms.selection.SelectionType;
import algorithms.factories.*;
import algorithms.io.MSRCPSPIO;
import algorithms.problem.BaseIndividual;
import algorithms.problem.scheduling.Schedule;
import algorithms.problem.scheduling.schedule_builders.ScheduleBuilderType;
import algorithms.quality_measure.GenerationalDistance;
import algorithms.quality_measure.HVMany;
import algorithms.quality_measure.InvertedGenerationalDistance;
import algorithms.quality_measure.Purity;
import distance_measures.Euclidean;
import interfaces.QualityMeasure;
import internal_measures.FlatBridgeSparsenessMeasure;
import internal_measures.FlatWithinPlusBetweenIndex;
import javafx.util.Pair;
import runners.CRSGARunnerHelper;
import util.FILE_OUTPUT_LEVEL;
import util.ParameterFunctions;
import util.random.RandomBase;
import util.random.RandomInt;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CRSGAMSRCPSP_BiObj_Runner extends CRSGARunnerHelper {
    private static final Logger LOGGER = Logger.getLogger(CRSGAMSRCPSP_BiObj_Runner.class.getName());
    private static final String baseDir = "." + File.separator;
    private static final String problemPath = "assets" + File.separator + "definitions" + File.separator + "MSRCPSP_fixed_idx" + File.separator;
    private static final String apfsPath = "." + File.separator + "apfs" + File.separator + "25-03-24" + File.separator;
    private static final List<Pair<String, String>> instanceWithOPF = Arrays.asList(
            new Pair<>(problemPath + "100_5_64_15.def", apfsPath + "100_5_64_15_merged.csv"),
            new Pair<>(problemPath + "100_10_47_9.def", apfsPath + "100_10_47_9_merged.csv"),
            new Pair<>(problemPath + "100_20_22_15.def", apfsPath + "100_20_22_15_merged.csv"),
            new Pair<>(problemPath + "200_10_84_9.def", apfsPath + "200_10_84_9_merged.csv"),
            new Pair<>(problemPath + "200_20_55_9.def", apfsPath + "200_20_55_9_merged.csv"),
            new Pair<>(problemPath + "200_40_133_15.def", apfsPath + "200_40_133_15_merged.csv")
    );

    public static void main(String[] args) {

        new CRSGAMSRCPSP_BiObj_Runner().run(args);
    }

    private List<BaseIndividual<Integer, Schedule>> run(String[] args) {
        for (int k = 0; k < instanceWithOPF.size(); k++) {
            String instanceName = instanceWithOPF.get(k).getKey();
            System.out.println("File " + (k + 1) + "/" + instanceWithOPF.size());

            Schedule schedule = readFile(k);
            if (schedule == null) return null;

            ParameterSet<Integer, Schedule> parameters = setParameters(schedule);
            List<BaseIndividual<Integer, Schedule>> optimalParetoFront = readAPF(instanceWithOPF.get(k).getValue(), schedule, parameters.evaluator);

            QualityMeasure[] clusterWeightMeasureList = new QualityMeasure[]{
//                new FlatCalinskiHarabasz(new Euclidean()), //this measures is sensitive to useSubtree toggle
//                new FlatDaviesBouldin(new Euclidean()), //this measures is sensitive to useSubtree toggle
//                new FlatDunn1(new Euclidean()), //this measures is sensitive to useSubtree toggle
//                new FlatDunn4(new Euclidean()), //this measures is sensitive to useSubtree toggle
//                    new FlatWithinBetweenIndex(new Euclidean()), //this measures is sensitive to useSubtree toggle
//                    new FlatWithinPlusBetweenIndex(new Euclidean()),
                    new FlatBridgeSparsenessMeasure(new Euclidean()),
//                new FlatDunn2(new Euclidean()),
//                new FlatDunn3(new Euclidean())
            };


            int NUMBER_OF_REPEATS = 2;
            int[] generationLimitList = new int[]{250_000};//{50_000};//{250_000};//{5_000};//{5_000};//{25_000, 12_500, 5_000, 2_500, 1_666, 1_250, 500, 250};//500};
            int[] populationSizeList = new int[]{1000};//{1000}{750}{25} {50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675, 700, 725, 750, 775, 800, 825, 850, 875, 900, 925, 950, 975, 1000};//{450, 550, 650, 750, 850, 1000};//{700};//{225, 300, 400, 550, 650, 750, 850}; //{10};//{5000, 6000, 7000}; //{10};//{10, 50, 100, 150, 500}; //{10};//{10};//{20};//{10, 100};//{20};//{10, 20, 50, 100};//{50};// 100};
            InitialPopulationType[] initialPopulationTypeList = new InitialPopulationType[]{InitialPopulationType.RANDOM};//, InitialPopulationType.NAIVE_SWAPS, InitialPopulationType.DIVERSITY, InitialPopulationType.OPPOSITION, InitialPopulationType.OPPOSITION_INT, InitialPopulationType.EVEN, InitialPopulationType.SHUFFLE};
//            ScheduleBuilderType[] ScheduleBuilderTypeList = new ScheduleBuilderType[] {ScheduleBuilderType.FORWARD_SCHEDULE_BUILDER, ScheduleBuilderType.BACKWARD_SCHEDULE_BUILDER};
            ScheduleBuilderType[] ScheduleBuilderTypeList = new ScheduleBuilderType[]{ScheduleBuilderType.FORWARD_SCHEDULE_BUILDER};
            double[] mutationProbabilityList = new double[]{0.01};//{0.0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};//{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};//{0.6};//{0.0, 0.001, 0.005, 0.01, 0.015, 0.02, 0.03, 0.05, 0.07, 0.1};//\0.5};//{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.5};//{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.25};//{0.0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};//{0.25};//{0.3};//{0.4};//}{0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, {0.4};//{0.4};//{0.1, 0.2, 0.3, 0.4, 0.5};//{0.01};//{0.007};//{0.002, 0.004, 0.006, 0.008};//{0.004};//{0.0, 0.0001, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.9};//{0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.0, 0.0001, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6}; //{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
            double[] crossoverProbabilityList = new double[]{0.45};//{0.45}{0.3};//{0.75};//{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.75};//{0.3, 0.35};//{0.35, 0.4};//{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.6};//{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.6};//{0.0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};//{0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};//{0.4};//{0.0, 0.1, 0.3, 0.5, 0.7, 0.9};//{0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};//}{0.0, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};//{0.45};{0.8};//}{0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};//{0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//{0.8};//{0.2};//{0.2};//{0.0, 0.05, 0.1, 0.15, 0.2}; //{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
            int[] numberOfClusterList = new int[]{3};//{3}; //{11, 12, 13, 14, 16, 17, 18, 19, 21, 22, 23, 24, 25};//{2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20};//{5};//{2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 17, 20, 22, 25, 30};//{5};//{2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 17, 20, 22, 25, 30};//{2};//{2, 3, 4, 5, 10, 20};//{3};
            int[] clusterisationAlgorithmIterList = new int[]{100};//100};
            /*if negative, it will disable that function */
            int[] clusteringRunFrequencyInCostList = {250};//{5, 10, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675, 700, 725, 750, 775, 800, 825, 850, 875, 900, 925, 950, 975, 1000, 1025, 1050, 1075, 1100, 1125, 1150, 1175, 1200, 1225, 1250, 1275, 1300, 1325, 1350, 1375, 1400, 1425, 1450, 1475, 1500, 1525, 1550, 1575, 1600, 1625, 1650, 1675, 1700, 1725, 1750, 1775, 1800, 1825, 1850, 1875, 1900, 1925, 1950, 1975, 2000};//{10, 75, 150, 200, 250, 300, 350, 450, 550, 650, 750, 850, 950};//{250};//{5, 20, 60, 100};//{10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 125, 130, 135, 140, 145, 150, 155, 160, 165, 170, 175, 180, 185, 190, 195, 200};//, 250, 500, 750, 1000, 2500, 5000, 7500, 10000, 15000, 20000, 30000, 50000};
            boolean[] isRecalculateCentresList = {false};
            boolean[] isClusteringEveryXCostList = {true};
            boolean[] isPopulationUsedList = {true};
            double[] edgeClustersDispersion = new double[]{3};//{3, 3.5};//{3.0, 2.5, 3.5};//{/*0.5, 1.0, */2.0/*, 3.0, 5.0, 10.0*/};//3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5};//{2};//{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10, 20, 50, 1000};//{2.5};//{0.0, 0.5, 1.5, 2.5, 3.5, 4.5, 7.0};//{4.0};//{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10, 20, 50, 1000};//{4.0};//{0.5, 1.0, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 20, 50};//{4.0};//{0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 20, 50};//{4};//{0.5};//{4};//{0.1, 0.5, 1, 2, 4, 10, 100};//{4}//{0.05, 0.1, 0.3, 0.5, 0.7, 0.9, 1, 4, 5, 10.0, 50, 100, 1_000, 5_000}; //{4};//, 10_000, 15_000, 20_000, 50_000, 100_000};//{0.1, 0.2, 0.3, 0.4, 0.5, 1.0, 1.5, 2.0};//{0.5, 1.0, 1.5, 2.0}; //}{0.1, 0.2, 0.3, 0.4, 0.5, 1.0, 1.5, 2.0};
            int[] tournamentSizeList = new int[]{3};//{3,2, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100}; //{10, 30, 50, 70/*, 90, 100*/}; //{500};//{70}; // {50};//{10, 20, 30, 40, 50}; //{15};//{10, 5, 15}; //{100};//{60};//{20, 40, 60, 80, 100}; //{0.95};////{200};//{10, 30, 50, 70, 90, 120, 200}; //{150};//{60, 70, 80, 90, 100}; //{80};//{10};//{80};//{10, 20, 30, 40, 50, 60, 70, 80, 90, 100}; //{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 50, 100}; //{90};
            int[] populationTurPropList = new int[]{100};//NJ //{50};
            MutationType[] mutationList = new MutationType[]{MutationType.RANDOM_BIT};
//            CrossoverType[] crossoverList = new CrossoverType[] {CrossoverType.BINOMIAL, CrossoverType.EXPONENTIAL, CrossoverType.SINGLE_POINT, CrossoverType.UNIFORM};
            CrossoverType[] crossoverList = new CrossoverType[]{CrossoverType.BINOMIAL};
            int[] indExclusionUsageLimitList = new int[]{100};//{50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000};//{250_000};//{750};//{300, 400, 500, 600, 700, 800, 900, 1000};//{250};//{100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};//{550, 600, 650, 700, 750, 800, 850, 900, 950, 1000};//}{50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000};//{50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 1000};//{25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675, 700};
            int[] indExclusionGenDurationList = new int[]{800};//{50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000};//{250_000};//{650};//{50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000};//}{150};//{100, 300, 500, 700, 900};//{150};//{{550};//{520, 540, 560, 580, 600, 620, 640, 660, 680};//{50, 150, 250, 350, 450, 550, 650};//{50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600};
            double[] turDecayParamList = new double[]{-5};//NJ //{-0.5, -1.5, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12.5, -13  .5, -14.5, -15.5};//{-6, -8, -15, -100};
            double[] localSearchPropList = {0.0};//NJ//{0.02, 0.03, 0.04, 0.05};//{0.001};//{0.001, 0.005, 0.01, 0.03, 0.06, 0.1};//{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};//
            double[] localSearchMutationPropList = {0.0};//NJ //{1.0, 0.0};//{0.001};//{0.0, 0.1, 0.3, 0.6, 1.0};//
            /*if negative, no decay will be applied!*/
            int[] minTournamentSizeList = new int[]{-666};//NJ //{3}//{-666};//{15};//{30, 40, 50, 60, 70};
            IndividualsPairingMethod[] individualsPairingMethodsList = new IndividualsPairingMethod[]{IndividualsPairingMethod.DISTANT_SPARSE_CLISTER_BRIDGE_PAIRS};//, IndividualsPairingMethod.ALL_POSSIBLE_PAIRS, IndividualsPairingMethod.DISTANT_IMMEDIATE_NEIGHBOUR_PAIR, IndividualsPairingMethod.CROSS_CLUSTER_ALL_POSSIBLE_PAIRS};//ALL_POSSIBLE_PAIRS CROSS_CLUSTER_ALL_POSSIBLE_PAIRS DISTANT_IMMEDIATE_NEIGHBOUR_PAIR DISTANT_IMMEDIATE_NEIGHBOUR_PAIR_SIMPLIFIED
            double[] minMaArchChangesThresholdList = {30};//{2, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100}; //{/*0, 10,*/ 20, 50, 100, 150, 200, 250};//{-666.0};//{290};
            double[] maxMaArchChangesThresholdList = {350};//{20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300, 310, 320, 330, 340, 350, 360, 370, 380, 390, 400}; //{250, 325, 400};//{0, 5, 10, 15, 20, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 500};//{Double.MAX_VALUE};//{320};
            int[] maArchChangesSizeList = {30};//{1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 25, 30, 35, 40, 45, 50, 55, 60};//{5, 10, 15};

            boolean[] isAllowDuplicateSelectionList = {true};
            boolean[] isSimulatedAnnealingList = {false};
            double[] initialTemperatureList = new double[]{100.0};
            double[] temperatureDecayRateList = new double[]{0.006};

            int[] minIndividualsList = new int[]{100};
            double[] choseTopPercentList = new double[]{0.3};

            int[] TopXList = new int[]{150};


            boolean shuffleParams = true;
            FILE_OUTPUT_LEVEL saveResultFiles = FILE_OUTPUT_LEVEL.MINIMAL;//FILE_OUTPUT_LEVEL.NONE FILE_OUTPUT_LEVEL.ALL FILE_OUTPUT_LEVEL.MINIMAL FILE_OUTPUT_LEVEL.REASONABLE;
            String summaryOutputFileName = "25-04-05_MS-RCPSP.csv";


            if (shuffleParams) {
                generationLimitList = shuffleIntArray(generationLimitList, parameters.random);
                populationSizeList = shuffleIntArray(populationSizeList, parameters.random);
                mutationProbabilityList = shuffleDoubleArray(mutationProbabilityList, parameters.random);
                crossoverProbabilityList = shuffleDoubleArray(crossoverProbabilityList, parameters.random);
                numberOfClusterList = shuffleIntArray(numberOfClusterList, parameters.random);
                clusterisationAlgorithmIterList = shuffleIntArray(clusterisationAlgorithmIterList, parameters.random);
                edgeClustersDispersion = shuffleDoubleArray(edgeClustersDispersion, parameters.random);
                tournamentSizeList = shuffleIntArray(tournamentSizeList, parameters.random);
                minTournamentSizeList = shuffleIntArray(minTournamentSizeList, parameters.random);
                populationTurPropList = shuffleIntArray(populationTurPropList, parameters.random);
                indExclusionUsageLimitList = shuffleIntArray(indExclusionUsageLimitList, parameters.random);
                indExclusionGenDurationList = shuffleIntArray(indExclusionGenDurationList, parameters.random);
                turDecayParamList = shuffleDoubleArray(turDecayParamList, parameters.random);
                clusteringRunFrequencyInCostList = shuffleIntArray(clusteringRunFrequencyInCostList, parameters.random);
                isRecalculateCentresList = shuffleBooleanArray(isRecalculateCentresList, parameters.random);
                isClusteringEveryXCostList = shuffleBooleanArray(isClusteringEveryXCostList, parameters.random);
                localSearchMutationPropList = shuffleDoubleArray(localSearchMutationPropList, parameters.random);
                localSearchPropList = shuffleDoubleArray(localSearchPropList, parameters.random);
                minMaArchChangesThresholdList = shuffleDoubleArray(minMaArchChangesThresholdList, parameters.random);
                maxMaArchChangesThresholdList = shuffleDoubleArray(maxMaArchChangesThresholdList, parameters.random);
                maArchChangesSizeList = shuffleIntArray(maArchChangesSizeList, parameters.random);
            }


            int numberOfParamConfigs = clusterWeightMeasureList.length * generationLimitList.length * populationSizeList.length * mutationProbabilityList.length
                    * crossoverProbabilityList.length * numberOfClusterList.length * clusterisationAlgorithmIterList.length * edgeClustersDispersion.length * tournamentSizeList.length
                    * populationTurPropList.length * mutationList.length * crossoverList.length * indExclusionUsageLimitList.length
                    * indExclusionGenDurationList.length * turDecayParamList.length * clusteringRunFrequencyInCostList.length * minTournamentSizeList.length * individualsPairingMethodsList.length * isRecalculateCentresList.length
                    * isClusteringEveryXCostList.length * isPopulationUsedList.length * localSearchMutationPropList.length * localSearchPropList.length * minMaArchChangesThresholdList.length
                    * maxMaArchChangesThresholdList.length * maArchChangesSizeList.length * initialPopulationTypeList.length * ScheduleBuilderTypeList.length;
            System.out.println("Number of param configurations: " + numberOfParamConfigs);
            String header = "dataset;counter;measure;no of repeats;avgIGD;stdev;uber pareto purity;runs with purity;mnd;tpfs;uber pareto IGD;uber pareto GD"
                    + ";uber pareto HV;uber pareto size;avgPurity;stdev;avgGD;stdev;avgHV;stdev;avgPFS;stdev"
                    + ";AvgAfterCrossParentDominationCounter"
                    + ";AvgAfterCrossParentDominationProp"
                    + ";AvgAfterCrossAndMutParentDominationCounter"
                    + ";AvgAfterCrossAndMutParentDominationProp"
                    + ";AvgAfterCrossAfterCrossAndMutDominationCounter"
                    + ";AvgAfterCrossAfterCrossAndMutDominationProp"
                    + ";AvgAfterCrossAndMutAfterCrossDominationCounter"
                    + ";AvgAfterCrossAndMutAfterCrossDominationProp"
                    + ";generationLimit;populationSize;initialPopMethod;ScheduleBuilderType;mutationProbability" +
                    ";crossoverProbability" +
                    ";localSearchArchiveProp;localSearchProp;numberOfClusters" +
                    ";clusterIterLimit;isClusteringEveryXCostEnabled;isCentresRecalculated;clusteringRunFrequencyInCost" +
                    ";isPopulationUsed;edgeClustersProb;tournamentSize;populationTurProp" +
                    ";mutationVersion;crossoverVersion" +
                    ";minMaArchChangesThreshold;maxMaArchChangesThreshold;maArchChangesSize;avgExtraPopulationTriggered;avgExtraPopulationTriggeredStdev" +
                    ";indExclusionUsageLimit;indExclusionGenDuration" +
                    ";turDecayParam;minTournamentSize;IndPairing";

            System.out.println(header);
            try {
                File f = new File(baseDir + summaryOutputFileName);
                if (!f.exists()) {
                    FileWriter fw = new FileWriter(baseDir + summaryOutputFileName, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(header);
                    bw.newLine();
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int paramCounter = 0;
            for (int wmNum = 0; wmNum < clusterWeightMeasureList.length; wmNum++) {
                QualityMeasure clusterWeightMeasure = clusterWeightMeasureList[wmNum];
                for (int i = 0; i < generationLimitList.length; i++) {
                    int generationLimit = generationLimitList[i];
                    for (int j = 0; j < populationSizeList.length; j++) {
                        int populationSize = populationSizeList[j];
                        for (int l = 0; l < mutationProbabilityList.length; l++) {
                            double mutationProbability = mutationProbabilityList[l];
                            for (int y = 0; y < crossoverProbabilityList.length; y++) {
                                double crossoverProbability = crossoverProbabilityList[y];
                                for (int n = 0; n < numberOfClusterList.length; n++) {
                                    int numberOfClusters = numberOfClusterList[n];
                                    for (int o = 0; o < clusterisationAlgorithmIterList.length; o++) {
                                        int clusterIterLimit = clusterisationAlgorithmIterList[o];
                                        for (int p = 0; p < edgeClustersDispersion.length; p++) {
                                            double edgeClustersDispVal = edgeClustersDispersion[p];
                                            for (int q = 0; q < tournamentSizeList.length; q++) {
                                                int tournamentSize = tournamentSizeList[q];
                                                for (int r = 0; r < populationTurPropList.length; r++) {
                                                    int populationTurProp = populationTurPropList[r];
                                                    for (int ii = 0; ii < mutationList.length; ii++) {
                                                        MutationType mutationType = mutationList[ii];
                                                        for (int iii = 0; iii < crossoverList.length; iii++) {
                                                            CrossoverType crossoverType = crossoverList[iii];
                                                            for (int kk = 0; kk < indExclusionUsageLimitList.length; kk++) {
                                                                int indExclusionUsageLimit = indExclusionUsageLimitList[kk];
                                                                for (int ll = 0; ll < indExclusionGenDurationList.length; ll++) {
                                                                    int indExclusionGenDuration = indExclusionGenDurationList[ll];
                                                                    for (int mm = 0; mm < turDecayParamList.length; mm++) {
                                                                        double turDecayParam = turDecayParamList[mm];
                                                                        for (int nn = 0; nn < minTournamentSizeList.length; nn++) {
                                                                            int minTournamentSize = minTournamentSizeList[nn];
                                                                            for (int oo = 0; oo < individualsPairingMethodsList.length; oo++) {
                                                                                IndividualsPairingMethod indPairingMethod = individualsPairingMethodsList[oo];
                                                                                for (int pp = 0; pp < clusteringRunFrequencyInCostList.length; pp++) {
                                                                                    int clusteringRunFrequencyInCost = clusteringRunFrequencyInCostList[pp];
                                                                                    for (int qq = 0; qq < isRecalculateCentresList.length; qq++) {
                                                                                        boolean isRecalculateCentres = isRecalculateCentresList[qq];
                                                                                        for (int rr = 0; rr < isClusteringEveryXCostList.length; rr++) {
                                                                                            boolean isClusteringEveryXCost = isClusteringEveryXCostList[rr];
                                                                                            for (int ss = 0; ss < isPopulationUsedList.length; ss++) {
                                                                                                boolean isPopulationUsed = isPopulationUsedList[ss];
                                                                                                for (int tt = 0; tt < localSearchMutationPropList.length; tt++) {
                                                                                                    double localSearchMutationProp = localSearchMutationPropList[tt];
                                                                                                    for (int vv = 0; vv < localSearchPropList.length; vv++) {
                                                                                                        double localSearchProp = localSearchPropList[vv];
                                                                                                        for (int xx = 0; xx < minMaArchChangesThresholdList.length; xx++) {
                                                                                                            double minMaArchChangesThreshold = minMaArchChangesThresholdList[xx];
                                                                                                            for (int yy = 0; yy < maxMaArchChangesThresholdList.length; yy++) {
                                                                                                                double maxMaArchChangesThreshold = maxMaArchChangesThresholdList[yy];
                                                                                                                if (minMaArchChangesThreshold > maxMaArchChangesThreshold) {
                                                                                                                    continue;
                                                                                                                }
                                                                                                                for (int zz = 0; zz < maArchChangesSizeList.length; zz++) {
                                                                                                                    int maArchChangesSize = maArchChangesSizeList[zz];
                                                                                                                    for (int aa = 0; aa < initialPopulationTypeList.length; aa++) {
                                                                                                                        InitialPopulationType initialPopulationType = initialPopulationTypeList[aa];
                                                                                                                        for (int bb = 0; bb < ScheduleBuilderTypeList.length; bb++) {
                                                                                                                            ScheduleBuilderType scheduleBuilderType = ScheduleBuilderTypeList[bb];
                                                                                                                            for (int cc = 0; cc < isAllowDuplicateSelectionList.length; cc++) {
                                                                                                                                boolean isAllowDuplicateSelection = isAllowDuplicateSelectionList[cc];
                                                                                                                                for (int dd = 0; dd < initialTemperatureList.length; dd++) {
                                                                                                                                    double initialTemperature = initialTemperatureList[dd];
                                                                                                                                    for (int ee = 0; ee < initialTemperatureList.length; ee++) {
                                                                                                                                        double temperatureDecayRate = temperatureDecayRateList[ee];
                                                                                                                                        for (int ff = 0; ff < isAllowDuplicateSelectionList.length; ff++) {
                                                                                                                                            boolean SimulatedAnnealingUsed = isSimulatedAnnealingList[ff];
                                                                                                                                            for (int gg = 0; gg < isAllowDuplicateSelectionList.length; gg++) {
                                                                                                                                                int minIndividuals = minIndividualsList[gg];
                                                                                                                                                for (int hh = 0; hh < isAllowDuplicateSelectionList.length; hh++) {
                                                                                                                                                    double choseTopPercent = choseTopPercentList[hh];
                                                                                                                                                    for (int kkk = 0; kkk < isAllowDuplicateSelectionList.length; kkk++) {
                                                                                                                                                        int TopX = TopXList[kkk];
                                                                                                                                            var eachRepeatHV = new ArrayList<Double>();
                                                                                                                                            var eachRepeatND = new ArrayList<Integer>();
                                                                                                                                            var eachRepeatOptimisationResult = new ArrayList<OptimisationResult>();
                                                                                                                                            var eachRepeatResult = new ArrayList<List<BaseIndividual<Integer, Schedule>>>();
                                                                                                                                            var eachRepeatIGD = new ArrayList<Double>();
                                                                                                                                            var eachRepeatGD = new ArrayList<Double>();
                                                                                                                                            var eachRepeatPurity = new ArrayList<Double>();
                                                                                                                                            List<Integer> eachRepeatNumberOfExtraPopulationTriggered = new ArrayList<>();
                                                                                                                                            paramCounter += 1;

                                                                                                                                            List<BaseIndividual<Integer, Schedule>> bestAPF = null;
                                                                                                                                            double bestAPFHV = -Double.MIN_VALUE;

                                                                                                                                            String outputFilename = "." + File.separator + "out" + File.separator
                                                                                                                                                    + removePrefixAndPostFixFromFileName(problemPath, ".def", instanceWithOPF.get(k).getKey())
                                                                                                                                                    + "_m-" + clusterWeightMeasure.getName()
                                                                                                                                                    + "_g" + generationLimit + "_p" + populationSize + "_pm" + initialPopulationType
                                                                                                                                                    + "_m" + mutationProbability + "_c" + crossoverProbability
                                                                                                                                                    + "_cN" + numberOfClusters + "_cI" + clusterIterLimit + "_edgC"
                                                                                                                                                    + edgeClustersDispVal + "_t" + tournamentSize + "_popT" + populationTurProp
                                                                                                                                                    + "_eL" + indExclusionUsageLimit + "_eg" + indExclusionGenDuration + "_d"
                                                                                                                                                    + turDecayParam + "_mt" + minTournamentSize + "_" + indPairingMethod.getName()
                                                                                                                                                    + "_m" + mutationType + "_c" + crossoverType + "_cc" + isClusteringEveryXCost
                                                                                                                                                    + "_cf" + clusteringRunFrequencyInCost + "_cr" + isRecalculateCentres + "_p" + isPopulationUsed
                                                                                                                                                    + "_l" + localSearchMutationProp + "_ls" + localSearchProp
                                                                                                                                                    + "_ma" + minMaArchChangesThreshold + "_" + maxMaArchChangesThreshold + "_" + maArchChangesSize
                                                                                                                                                    + "_sb" + scheduleBuilderType;

                                                                                                                                            String bestAPFoutputFile = "bestAPF";
                                                                                                                                            int bestIterNumber = 0;

                                                                                                                                            File theDir = new File(outputFilename);
                                                                                                                                            if (!theDir.exists()) {
                                                                                                                                                theDir.mkdirs();
                                                                                                                                            }

                                                                                                                                            List<BaseIndividual<Integer, Schedule>> uberPareto = new ArrayList<>();
                                                                                                                                            List<BaseIndividual<Integer, Schedule>> optimalApfWithUberPareto = new ArrayList<>();
                                                                                                                                            CRSGA_MSRCPSP<Schedule> geneticAlgorithm = null;
                                                                                                                                            for (int xxx = 0; xxx < NUMBER_OF_REPEATS; xxx++) {
                                                                                                                                                parameters.mutation = new MutationFactory(parameters).createMutation(mutationType);
                                                                                                                                                parameters.crossover = new CrossoverFactory().createCrossover(crossoverType);
                                                                                                                                                parameters.scheduleBuilder = new ScheduleBuilderFactory(parameters).createScheduleBuilder(scheduleBuilderType);
                                                                                                                                                parameters.initialPopulation = new InitialPopulationGeneratorFactory(parameters).createInitialPopulation(initialPopulationType);
                                                                                                                                                parameters.localSearchMutationProp = localSearchMutationProp;
                                                                                                                                                parameters.localSearchOverallProp = localSearchProp;

                                                                                                                                                HVMany hv = new HVMany(parameters.evaluator.getNadirPoint());

                                                                                                                                                geneticAlgorithm = new CRSGA_MSRCPSP<Schedule>(
                                                                                                                                                        schedule,
                                                                                                                                                        clusterWeightMeasure,
                                                                                                                                                        populationSize,
                                                                                                                                                        generationLimit,
                                                                                                                                                        parameters,
                                                                                                                                                        mutationProbability,
                                                                                                                                                        crossoverProbability,
                                                                                                                                                        instanceWithOPF.get(k).getKey().split("\\.")[0],
                                                                                                                                                        numberOfClusters,
                                                                                                                                                        clusterIterLimit,
                                                                                                                                                        edgeClustersDispVal,
                                                                                                                                                        tournamentSize,
                                                                                                                                                        populationTurProp,
                                                                                                                                                        hv,
                                                                                                                                                        optimalParetoFront,
                                                                                                                                                        outputFilename,
                                                                                                                                                        saveResultFiles,
                                                                                                                                                        xxx,
                                                                                                                                                        indExclusionUsageLimit,
                                                                                                                                                        indExclusionGenDuration,
                                                                                                                                                        turDecayParam,
                                                                                                                                                        minTournamentSize,
                                                                                                                                                        indPairingMethod,
                                                                                                                                                        clusteringRunFrequencyInCost,
                                                                                                                                                        isClusteringEveryXCost,
                                                                                                                                                        isRecalculateCentres,
                                                                                                                                                        isPopulationUsed,
                                                                                                                                                        minMaArchChangesThreshold,
                                                                                                                                                        maxMaArchChangesThreshold,
                                                                                                                                                        maArchChangesSize,
                                                                                                                                                        isAllowDuplicateSelection,
                                                                                                                                                        initialTemperature,
                                                                                                                                                        temperatureDecayRate,
                                                                                                                                                        SimulatedAnnealingUsed,
                                                                                                                                                        minIndividuals,
                                                                                                                                                        choseTopPercent,
                                                                                                                                                        TopX
                                                                                                                                                );

                                                                                                                                                var result = geneticAlgorithm.optimize();
                                                                                                                                                eachRepeatNumberOfExtraPopulationTriggered.add(geneticAlgorithm.getNumberOfExtraPopulationTriggered());
                                                                                                                                                geneticAlgorithm.removeDuplicatesAndDominated(result, uberPareto);
//                    uberPareto = geneticAlgorithm.getNondominatedFromTwoLists(result, uberPareto);
                                                                                                                                                //            printResults(result);

                                                                                                                                                eachRepeatOptimisationResult.add(geneticAlgorithm.getOptimisationResult());
                                                                                                                                                eachRepeatResult.add(result);

                                                                                                                                                String instanceNameForFile = removePrefixAndPostFixFromFileName(problemPath, ".def", instanceName);
                                                                                                                                                if (saveResultFiles.getLevel() >= 1) {
                                                                                                                                                    try {
                                                                                                                                                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename
                                                                                                                                                                + File.separator + instanceNameForFile + "_config0_run" + xxx + "_archive.csv"));
                                                                                                                                                        writer.write(printResultsForComparison(result, parameters.objectiveNames, false));
                                                                                                                                                        writer.close();
                                                                                                                                                    } catch (
                                                                                                                                                            IOException e) {
                                                                                                                                                        e.printStackTrace();
                                                                                                                                                    }
                                                                                                                                                }

                                                                                                                                                System.out.print(xxx + ", ");
                                                                                                                                            }
                                                                                                                                            System.out.println("");

                                                                                                                                            optimalApfWithUberPareto = new ArrayList<>(optimalParetoFront);
                                                                                                                                            geneticAlgorithm.removeDuplicatesAndDominated(uberPareto, optimalApfWithUberPareto);

                                                                                                                                            int mnd = geneticAlgorithm.getNumberOfNotDominated(uberPareto, optimalApfWithUberPareto);

                                                                                                                                            Pair<Pair<List<BaseIndividual<Integer, Schedule>>, List<BaseIndividual<Integer, Schedule>>>
                                                                                                                                                    , ArrayList<List<BaseIndividual<Integer, Schedule>>>> normalisedApfAndResults
                                                                                                                                                    = normaliseParetoFrontsByMinMax(optimalApfWithUberPareto, uberPareto, eachRepeatResult, schedule,
                                                                                                                                                    parameters.evaluator);
                                                                                                                                            List<BaseIndividual<Integer, Schedule>> normalisedOptimalPftWithUberPareto = normalisedApfAndResults.getKey().getKey();
                                                                                                                                            List<BaseIndividual<Integer, Schedule>> normalisedUberPareto = normalisedApfAndResults.getKey().getValue();
                                                                                                                                            ArrayList<List<BaseIndividual<Integer, Schedule>>> normalisedResults = normalisedApfAndResults.getValue();

//                        optimalApfWithUberPareto = geneticAlgorithm.getNondominatedFromTwoLists(optimalParetoFront, uberPareto);
                                                                                                                                            InvertedGenerationalDistance igdCalculator = new InvertedGenerationalDistance(normalisedOptimalPftWithUberPareto);
                                                                                                                                            GenerationalDistance gdCalculator = new GenerationalDistance(normalisedOptimalPftWithUberPareto);
                                                                                                                                            Purity purityCalculator = new Purity(normalisedOptimalPftWithUberPareto);

                                                                                                                                            BaseIndividual<Integer, Schedule> normalisedHvNadirPoint = new BaseIndividual<>(schedule, new ArrayList<>(), parameters.evaluator);
                                                                                                                                            //FIXME: adjust for more objective values
                                                                                                                                            normalisedHvNadirPoint.setObjectives(new double[]{1.0, 1.0});
                                                                                                                                            normalisedHvNadirPoint.setNormalObjectives(new double[]{1.0, 1.0});
                                                                                                                                            normalisedHvNadirPoint.setHashCode();
                                                                                                                                            HVMany hvCalculator = new HVMany(normalisedHvNadirPoint);

                                                                                                                                            for (int yyy = 0; yyy < normalisedResults.size(); yyy++) {
                                                                                                                                                var normRes = normalisedResults.get(yyy);
                                                                                                                                                var result = eachRepeatResult.get(yyy);

                                                                                                                                                var hvValue = hvCalculator.getMeasure(normRes);
                                                                                                                                                eachRepeatHV.add(hvValue);
                                                                                                                                                eachRepeatND.add(result.size());

                                                                                                                                                if (hvValue > bestAPFHV) {
                                                                                                                                                    bestAPFHV = hvValue;
                                                                                                                                                    bestAPF = result;
                                                                                                                                                    bestIterNumber = yyy;
                                                                                                                                                }

                                                                                                                                                var igdValue = igdCalculator.getMeasure(normRes);
                                                                                                                                                eachRepeatIGD.add(igdValue);

                                                                                                                                                var gdValue = gdCalculator.getMeasure(normRes);
                                                                                                                                                eachRepeatGD.add(gdValue);

                                                                                                                                                var purityValue = purityCalculator.getMeasure(normRes);
                                                                                                                                                eachRepeatPurity.add(purityValue);
                                                                                                                                            }

                                                                                                                                            String instanceNameForFile = removePrefixAndPostFixFromFileName(problemPath, ".def", instanceName);
                                                                                                                                            try {
                                                                                                                                                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename
                                                                                                                                                        + File.separator + instanceNameForFile + "_UBER_PARETO.csv"));
                                                                                                                                                writer.write(printParetos("uber", uberPareto, "apf", optimalParetoFront, parameters.objectiveNames, false, false));
                                                                                                                                                writer.close();
                                                                                                                                            } catch (
                                                                                                                                                    IOException e) {
                                                                                                                                                e.printStackTrace();
                                                                                                                                            }

                                                                                                                                            if (saveResultFiles.getLevel() > 1) {
                                                                                                                                                try {
                                                                                                                                                    BufferedWriter writer = null;

                                                                                                                                                    writer = new BufferedWriter(new FileWriter(outputFilename
                                                                                                                                                            + File.separator + instanceNameForFile + "_apf.csv"));
                                                                                                                                                    writer.write(printParetos("uber", uberPareto, "uber+apf", optimalApfWithUberPareto, parameters.objectiveNames, false, false));
                                                                                                                                                    writer.close();

                                                                                                                                                    writer = new BufferedWriter(new FileWriter(outputFilename
                                                                                                                                                            + File.separator + instanceNameForFile + "_genes_UBER_PARETO.csv"));
                                                                                                                                                    writer.write(printGenes(uberPareto, schedule));
                                                                                                                                                    writer.close();
                                                                                                                                                } catch (
                                                                                                                                                        IOException e) {
                                                                                                                                                    e.printStackTrace();
                                                                                                                                                }
                                                                                                                                            }

                                                                                                                                            OptionalDouble NDaverage = eachRepeatND
                                                                                                                                                    .stream()
                                                                                                                                                    .mapToDouble(a -> a)
                                                                                                                                                    .average();
                                                                                                                                            var avgPFS = NDaverage.isPresent() ? NDaverage.getAsDouble() : -666.0;

                                                                                                                                            double NDstandardDeviation = 0.0;
                                                                                                                                            for (double num : eachRepeatND) {
                                                                                                                                                NDstandardDeviation += Math.pow(num - avgPFS, 2);
                                                                                                                                            }
                                                                                                                                            NDstandardDeviation = Math.sqrt(NDstandardDeviation / eachRepeatND.size());

                                                                                                                                            OptionalDouble average = eachRepeatHV
                                                                                                                                                    .stream()
                                                                                                                                                    .mapToDouble(a -> a)
                                                                                                                                                    .average();
                                                                                                                                            var avgHV = average.isPresent() ? average.getAsDouble() : -666.0;

                                                                                                                                            double standardDeviation = 0.0;
                                                                                                                                            for (double num : eachRepeatHV) {
                                                                                                                                                standardDeviation += Math.pow(num - avgHV, 2);
                                                                                                                                            }

                                                                                                                                            standardDeviation = Math.sqrt(standardDeviation / eachRepeatHV.size());

                                                                                                                                            OptionalDouble averageIGD = eachRepeatIGD
                                                                                                                                                    .stream()
                                                                                                                                                    .mapToDouble(a -> a)
                                                                                                                                                    .average();
                                                                                                                                            var averageIGDVal = averageIGD.isPresent() ? averageIGD.getAsDouble() : -666.0;
                                                                                                                                            double averageIGDValStdev = 0.0;
                                                                                                                                            for (double num : eachRepeatIGD) {
                                                                                                                                                averageIGDValStdev += Math.pow(num - averageIGDVal, 2);
                                                                                                                                            }
                                                                                                                                            averageIGDValStdev = Math.sqrt(averageIGDValStdev / eachRepeatIGD.size());

                                                                                                                                            OptionalDouble averageGD = eachRepeatGD
                                                                                                                                                    .stream()
                                                                                                                                                    .mapToDouble(a -> a)
                                                                                                                                                    .average();
                                                                                                                                            var averageGDVal = averageGD.isPresent() ? averageGD.getAsDouble() : -666.0;
                                                                                                                                            double averageGDStdev = 0.0;
                                                                                                                                            for (double num : eachRepeatGD) {
                                                                                                                                                averageGDStdev += Math.pow(num - averageGDVal, 2);
                                                                                                                                            }
                                                                                                                                            averageGDStdev = Math.sqrt(averageGDStdev / eachRepeatGD.size());

                                                                                                                                            OptionalDouble averagePurity = eachRepeatPurity
                                                                                                                                                    .stream()
                                                                                                                                                    .mapToDouble(a -> a)
                                                                                                                                                    .average();
                                                                                                                                            var averagePurityVal = averagePurity.isPresent() ? averagePurity.getAsDouble() : -666.0;
                                                                                                                                            double averagePurityStdev = 0.0;
                                                                                                                                            for (double num : eachRepeatPurity) {
                                                                                                                                                averagePurityStdev += Math.pow(num - averagePurityVal, 2);
                                                                                                                                            }
                                                                                                                                            averagePurityStdev = Math.sqrt(averagePurityStdev / eachRepeatPurity.size());
                                                                                                                                            long runsWithPurity = eachRepeatPurity.stream().filter(value -> value > 0).count();

                                                                                                                                            OptionalDouble avgEachRepeatNumberOfExtraPopulationTriggered = eachRepeatNumberOfExtraPopulationTriggered
                                                                                                                                                    .stream()
                                                                                                                                                    .mapToDouble(a -> a)
                                                                                                                                                    .average();
                                                                                                                                            double avgEachRepeatNumberOfExtraPopulationTriggeredVal = avgEachRepeatNumberOfExtraPopulationTriggered.isPresent() ?
                                                                                                                                                    avgEachRepeatNumberOfExtraPopulationTriggered.getAsDouble() : -666.0;
                                                                                                                                            double avgEachRepeatNumberOfExtraPopulationTriggeredStdev = 0.0;
                                                                                                                                            for (int num : eachRepeatNumberOfExtraPopulationTriggered) {
                                                                                                                                                avgEachRepeatNumberOfExtraPopulationTriggeredStdev += Math.pow(num - avgEachRepeatNumberOfExtraPopulationTriggeredVal, 2);
                                                                                                                                            }
                                                                                                                                            avgEachRepeatNumberOfExtraPopulationTriggeredStdev = Math.sqrt(avgEachRepeatNumberOfExtraPopulationTriggeredStdev /
                                                                                                                                                    eachRepeatNumberOfExtraPopulationTriggered.size());

                                                                                                                                            String runResult = instanceNameForFile + ";" + paramCounter + "/" + numberOfParamConfigs + ";"
                                                                                                                                                    + clusterWeightMeasure.getClass().getName() + ";" + NUMBER_OF_REPEATS
                                                                                                                                                    + ";" + averageIGDVal + ";" + averageIGDValStdev
                                                                                                                                                    + ";" + purityCalculator.getMeasure(normalisedUberPareto)
                                                                                                                                                    + ";" + runsWithPurity
                                                                                                                                                    + ";" + mnd + ";" + optimalApfWithUberPareto.size()
                                                                                                                                                    + ";" + igdCalculator.getMeasure(normalisedUberPareto)
                                                                                                                                                    + ";" + gdCalculator.getMeasure(normalisedUberPareto)
//                        + ";" + new HVMany(parameters.evaluator.getNadirPoint()).getMeasure(normalisedUberPareto)
                                                                                                                                                    + ";" + hvCalculator.getMeasure(normalisedUberPareto)
                                                                                                                                                    + ";" + normalisedUberPareto.size()
                                                                                                                                                    + ";" + averagePurityVal + ";" + averagePurityStdev
                                                                                                                                                    + ";" + averageGDVal + ";" + averageGDStdev
                                                                                                                                                    + ";" + avgHV + ";" + standardDeviation
                                                                                                                                                    + ";" + avgPFS + ";" + NDstandardDeviation
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossParentDominationCounter(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossParentDominationProp(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossAndMutParentDominationCounter(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossAndMutParentDominationProp(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossAfterCrossAndMutDominationCounter(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossAfterCrossAndMutDominationProp(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossAndMutAfterCrossDominationCounter(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + OptimisationResult.getAvgAfterCrossAndMutAfterCrossDominationProp(eachRepeatOptimisationResult)
                                                                                                                                                    + ";" + generationLimit
                                                                                                                                                    + ";" + populationSize + ";" + initialPopulationType
                                                                                                                                                    + ";" + scheduleBuilderType + ";" + mutationProbability + ";" + crossoverProbability
                                                                                                                                                    + ";" + localSearchMutationProp + ";" + localSearchProp
                                                                                                                                                    + ";" + numberOfClusters + ";" + clusterIterLimit + ";" + isClusteringEveryXCost + ";" + isRecalculateCentres
                                                                                                                                                    + ";" + clusteringRunFrequencyInCost + ";" + isPopulationUsed + ";" + edgeClustersDispVal + ";" + tournamentSize
                                                                                                                                                    + ";" + populationTurProp + ";" + mutationType.name() + ";" + crossoverType.name()
                                                                                                                                                    + ";" + minMaArchChangesThreshold + ";" + maxMaArchChangesThreshold + ";" + maArchChangesSize
                                                                                                                                                    + ";" + avgEachRepeatNumberOfExtraPopulationTriggeredVal
                                                                                                                                                    + ";" + avgEachRepeatNumberOfExtraPopulationTriggeredStdev
                                                                                                                                                    + ";" + indExclusionUsageLimit + ";" + indExclusionGenDuration
                                                                                                                                                    + ";" + turDecayParam + ";" + minTournamentSize + ";" + indPairingMethod;
                                                                                                                                            System.out.println(runResult);
                                                                                                                                            try {
                                                                                                                                                FileWriter fw = new FileWriter(baseDir + summaryOutputFileName, true);
                                                                                                                                                BufferedWriter bw = new BufferedWriter(fw);
                                                                                                                                                bw.write(runResult);
                                                                                                                                                bw.newLine();
                                                                                                                                                bw.close();
                                                                                                                                            } catch (
                                                                                                                                                    IOException e) {
                                                                                                                                                e.printStackTrace();
                                                                                                                                            }

                                                                                                                                            if (saveResultFiles.getLevel() > 1) {
                                                                                                                                                try {
                                                                                                                                                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename + File.separator
                                                                                                                                                            + bestAPFoutputFile + bestIterNumber + ".csv"));
                                                                                                                                                    writer.write(printResultsForComparison(bestAPF, parameters.objectiveNames, false));
                                                                                                                                                    writer.close();
                                                                                                                                                } catch (
                                                                                                                                                        IOException e) {
                                                                                                                                                    e.printStackTrace();
                                                                                                                                                }
                                                                                                                                            }}}}
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int[] shuffleIntArray(int[] intArray, RandomBase<Integer> random) {
        for (int i = intArray.length - 1; i > 0; i--) {
            // Generate a random index between 0 and i (inclusive)
            int j = random.nextInt(i + 1);

            // Swap the elements at indices i and j
            int temp = intArray[i];
            intArray[i] = intArray[j];
            intArray[j] = temp;
        }

        return intArray;
    }

    private static double[] shuffleDoubleArray(double[] doubleArray, RandomBase<Integer> random) {
        for (int i = doubleArray.length - 1; i > 0; i--) {
            // Generate a random index between 0 and i (inclusive)
            int j = random.nextInt(i + 1);

            // Swap the elements at indices i and j
            double temp = doubleArray[i];
            doubleArray[i] = doubleArray[j];
            doubleArray[j] = temp;
        }

        return doubleArray;
    }

    public static boolean[] shuffleBooleanArray(boolean[] booleanArray, RandomBase<Integer> random) {
        for (int i = booleanArray.length - 1; i > 0; i--) {
            // Generate a random index between 0 and i (inclusive)
            int j = random.nextInt(i + 1);

            // Swap the elements at indices i and j
            boolean temp = booleanArray[i];
            booleanArray[i] = booleanArray[j];
            booleanArray[j] = temp;
        }

        return booleanArray;
    }

    public static double roundNumberToTwoDecimalPlaces(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    private static List<BaseIndividual<Integer, Schedule>> readAPF(String apfPath, Schedule schedule, BaseEvaluator<Integer, Schedule> evaluator) {
        File file = new File(apfPath);
        System.out.println("File;" + file.getName());
        List<BaseIndividual<Integer, Schedule>> front = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                double firstObj = Double.parseDouble(values[0]);
                double secondObj = Double.parseDouble(values[1]);
//                        System.out.println(firstObj + ", " + secondObj);

                BaseIndividual<Integer, Schedule> individual = new BaseIndividual<>(schedule, new ArrayList<>(), evaluator);
                individual.setObjectives(new double[]{firstObj, secondObj});
                individual.setHashCode();

                double normFirstObj = firstObj / ((BaseScheduleEvaluator<Integer>) evaluator).getMaxDuration();
                double normSecondObj = secondObj / ((BaseScheduleEvaluator<Integer>) evaluator).getMaxCost();
                individual.setNormalObjectives(new double[]{normFirstObj, normSecondObj});

                front.add(individual);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return front;
    }

    private static Schedule readFile(int k) {
        var definitionFile = baseDir + instanceWithOPF.get(k).getKey();

        MSRCPSPIO reader = new MSRCPSPIO();
        Schedule schedule = reader.readDefinition(definitionFile);
        if (null == schedule) {
            LOGGER.log(Level.WARNING, "Could not read the Definition " + definitionFile);
            return null;
        }
        return schedule;
    }

    private static ParameterSet<Integer, Schedule> setParameters(Schedule schedule) {
        ParameterSet<Integer, Schedule> parameters = new ParameterSet<>();
        parameters.objectiveNames = new String[]{"Duration", "Cost"};
        parameters.upperBounds = schedule.getUpperBounds();
//        parameters.random = new RandomInt(System.currentTimeMillis());
        parameters.random = new RandomInt(42);
        parameters.hasSuccesors = schedule.getSuccesors();
        parameters.populationMultiplicationFactor = 1;
        parameters.evalRate = 1.0;
        parameters.tournamentSize = 2;
        parameters.initialPopulation = new InitialPopulationGeneratorFactory(parameters).createInitialPopulation(InitialPopulationType.RANDOM);
        parameters.selection = new SelectionFactory(parameters).createSelection(SelectionType.NONDOMINATED_SORTING_TOURNAMENT);
        parameters.crossover = new CrossoverFactory().createCrossover(CrossoverType.SINGLE_POINT);
        parameters.mutation = new MutationFactory(parameters).createMutation(MutationType.RANDOM_BIT);
        parameters.converter = new ConverterFactory(parameters).createConverter(ConverterType.TRUNCATING);
        parameters.scheduleBuilder = new ScheduleBuilderFactory(parameters).createScheduleBuilder(ScheduleBuilderType.FORWARD_SCHEDULE_BUILDER);
        parameters.evaluator = new EvaluatorFactory().createEvaluator(EvaluatorType.BASE_SCHEDULE_EVALUATOR, parameters.evalRate);
        parameters.evaluator.setIndividual(new BaseIndividual<>(schedule, parameters.evaluator));
        return parameters;
    }

    private String printGenes(List<BaseIndividual<Integer, Schedule>> resultIndividuals, Schedule problem) {
        String output = "types;";
        for (int i = 0; i < problem.getTasks().length; i++) {
            output += "task" + i + ";";
        }
        output += "\n";

        for (int i = 0; i < resultIndividuals.size(); ++i) {
            BaseIndividual<Integer, Schedule> ind = resultIndividuals.get(i);
            output += "genotype(assigned resources);";
            for (int j = 0; j < ind.getGenes().size(); j++) {
                output += ind.getGenes().get(j) + ";";
            }
            output += "\ntask finish time;";
            for (int j = 0; j < ind.getGenes().size(); j++) {
                output += ind.getProblem().getResource(ind.getGenes().get(j)).getFinish() + ";";
            }
            output += "\n";
        }
        return output;
    }
}


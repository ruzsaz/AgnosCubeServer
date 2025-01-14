package hu.agnos.cube.server.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import hu.agnos.cube.Cube;
import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.driver.service.DataRetriever;
import hu.agnos.cube.driver.service.ProblemFactory;
import hu.agnos.cube.meta.queryDto.BaseVectorCoordinateForCube;
import hu.agnos.cube.meta.queryDto.CubeQuery;
import hu.agnos.cube.meta.queryDto.DrillVectorForCube;
import hu.agnos.cube.meta.resultDto.ResultElement;
import hu.agnos.cube.meta.resultDto.ResultSet;
import hu.agnos.cube.server.postprocessor.KaplanMeier;
import hu.agnos.cube.server.repository.CubeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to determine the results of a series of queries, based on the same baseVector.
 */
@Service
public class CubeService {

    @Autowired
    CubeRepo cubeRepo;

    @Autowired
    CacheCreator rzcreator;

    /**
     * Prepares the data cubes for the given cube names: loads the data if not present.
     *
     * @param cubeNames List of cube names to prepare
     */
    public void prepareCubes(List<String> cubeNames) {
        cubeNames.parallelStream().forEach(cubeRepo::getCubeAndLoadDataIfNotPresent);


        rzcreator.createCache(cubeNames.get(0));

    }

    /**
     * Determines the results of a series of queries from the CubeDriver parallel, and collects the answers.
     * The queries must be based on the same baseVector.
     *
     * @param queries List of queries to resolve; the queries should be based on the same baseVector
     * @return The answers for the queries
     */
    public List<ResultSet> getData(List<CubeQuery> queries) {
        List<ResultSet> resultSetList = new ArrayList<>(queries.size());
        Cube cube = cubeRepo.getCubeAndLoadDataIfNotPresent(queries.get(0).cubeName());
        for (CubeQuery query : queries) {
            DataRetriever retriever = CubeService.createDataRetriever(cube, query.baseVector(), query.drillVector());
            List<Future<ResultElement>> futures = retriever.computeAll();
            List<ResultElement> tempResult = CubeService.extractResults(futures);

            ResultSet resultSet = new ResultSet(cube.getName(),
                    cube.getDimensionHeader(),
                    Arrays.asList(cube.getMeasureHeader()),
                    query.originalDrill(),
                    query.drillVector().drillRequired(),
                    tempResult);
            resultSetList.add(resultSet);
        }
        return resultSetList.parallelStream().map(r -> CubeService.postProcess(cube, r)).toList();
    }

    /**
     * Extract the results of individual data-queries for the same drill, and collect them.
     *
     * @param futures List of futures containing ResultElements
     * @return A list of ResultElements for the drill.
     */
    static List<ResultElement> extractResults(List<Future<ResultElement>> futures) {
        List<ResultElement> resultElements = new ArrayList<>(futures.size());
        for (Future<ResultElement> future : futures) {
            try {
                ResultElement resultElement = future.get();
                resultElements.add(resultElement);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(CubeService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resultElements;
    }

    /**
     * Creates a collection of tasks to run to get the result for a drill request.
     * A task contains only one atomic data retrieval, along a single dimension-coordinate tuple.
     *
     * @param cube Cube to process
     * @param baseVector Base vector the drills are based in, personalized for the cube
     * @param drillVector The drill vector personalized for the cube
     * @return The collection of tasks, as a DataRetriever object
     */
    private static DataRetriever createDataRetriever(Cube cube, List<BaseVectorCoordinateForCube> baseVector, DrillVectorForCube drillVector) {
        DataRetriever retriever = new DataRetriever();
        ProblemFactory problemFactory = new ProblemFactory(cube);
        List<List<Node>> newBaseVectorArray = QueryGenerator.getCoordinatesOfDrill(cube.getDimensions(), cube.getPostCalculations(), baseVector, drillVector);
        for (List<Node> nodes : newBaseVectorArray) {
            retriever.addProblem(problemFactory.createProblem(nodes));
        }
        return retriever;
    }

    /**
     * Post-process (like Kaplan-Meier calculation) the result of a drill if required.
     * The input resultSet is modified, but the same is returned too.
     *
     * @param cube Cube to look for a post-processing directive
     * @param resultSet ResultSet to post-process
     * @return The post-calculated resultSet
     */
    private static ResultSet postProcess(Cube cube, ResultSet resultSet) {
        int dimIndex = CubeService.dimIndex(cube);
        if (dimIndex >= 0) {
            List<Integer> kaplanMeierIndices = KaplanMeier.getKaplanMeierIndicatorIndices(cube);
            KaplanMeier.process(resultSet, kaplanMeierIndices, dimIndex);
        }
        return resultSet;
    }

    /**
     * If post-calculation is required, determines the index of the post-calculation's dimension.
     *
     * @param cube The cube to look for post-calculations
     * @return The index of the dimension, or -1 if post-calculations are not required
     */
    private static int dimIndex(Cube cube) {
        if (!cube.getPostCalculations().isEmpty()) {
            Dimension dimension = cube.getPostCalculations().get(0).dimension();
            return cube.getDimensions().indexOf(dimension);
        }
        return -1;
    }

}

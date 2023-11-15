package hu.agnos.cube.server.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

@Service
public class CubeService {

    @Autowired
    CubeRepo cubeRepo;

    public ResultSet[] getData(CubeQuery query) {
        Cube cube = cubeRepo.getCube(query.cubeName());
        int numberOfDifferentDrills = query.originalDrills().size();

        DataRetriever retriever = createDataRetriever(cube, query.baseVector(), query.drillVectors(), numberOfDifferentDrills);
        List<Future<ResultElement>> futures = retriever.computeAll();
        List<List<ResultElement>> tempResult = extractResults(futures, numberOfDifferentDrills);

        ResultSet[] resultSets = new ResultSet[numberOfDifferentDrills];
        for (int i = 0; i < numberOfDifferentDrills; i++) {
            resultSets[i] = new ResultSet(cube.getName(),
                    cube.getDimensionHeader(),
                    Arrays.asList(cube.getMeasureHeader()),
                    query.originalDrills().get(i),
                    query.drillVectors().get(i).drillRequired(),
                    tempResult.get(i));
        }
        return CubeService.postProcess(cube, resultSets);
    }

    /**
     * Extract the results of individual data-queries, and collect them
     * to the original drills
     *
     * @param futures List of future ResultElements
     * @param numberOfDifferentDrills Number of drills the Futures were made from
     * @return A list of ResultElements for each original drill, as a List with the same
     * order the original drills made in.
     */
    private List<List<ResultElement>> extractResults(List<Future<ResultElement>> futures, int numberOfDifferentDrills) {
        List<List<ResultElement>> tempResult = new ArrayList<>();
        for (int i = 0; i < numberOfDifferentDrills; i++) {
            List<ResultElement> tempResultForDrill = new ArrayList<>();
            tempResult.add(tempResultForDrill);
        }

        for (Future<ResultElement> future : futures) {
            try {
                ResultElement resultElement = future.get();
                int drillVectorId = resultElement.drillVectorId();
                List<ResultElement> tempResultForDrill = tempResult.get(drillVectorId);
                tempResultForDrill.add(resultElement);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(CubeService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tempResult;
    }

    /**
     * Creates a collection of tasks to run to get the results for all the drill request.
     * A task contains only one data retrieval, along a single dimension-coordinate tuple.
     *
     * @param cube Cube to process
     * @param baseVector Base vector the drills are based in, personalized for the cube
     * @param drillVectors The drill vectors personalized for the cube
     * @param numberOfDrills Number of original drills
     * @return The collection of tasks, as a DataRetriever object
     */
    private DataRetriever createDataRetriever(Cube cube, List<BaseVectorCoordinateForCube> baseVector, List<DrillVectorForCube> drillVectors, int numberOfDrills) {
        DataRetriever retriever = new DataRetriever();
        ProblemFactory problemFactory = new ProblemFactory(cube);
        for (int i = 0; i < numberOfDrills; i++) {
            DrillVectorForCube drillVector = drillVectors.get(i);
            Optional<List<List<Node>>> newBaseVectorArray = QueryGenerator.getCoordinatesOfDrill(cube.getDimensions(), cube.getPostCalculations(), baseVector, drillVector);

            if (newBaseVectorArray.isPresent()) {
                for (List<Node> nodes : newBaseVectorArray.get()) {
                    retriever.addProblem(problemFactory.createProblem(i, nodes));
                }
            }
        }
        return retriever;
    }

    // TODO: párhuzamosítani
    private static ResultSet[] postProcess(Cube cube, ResultSet[] resultSets) {
        int dimIndex = CubeService.dimIndex(cube);
        if (dimIndex >= 0) {
            List<Integer> kaplanMeierIndices = KaplanMeier.getKaplanMeierIndicatorIndices(cube);
            for (ResultSet r : resultSets) {
                KaplanMeier.process(r, kaplanMeierIndices, dimIndex);
            }
        }
        return resultSets;
    }

    private static int dimIndex(Cube cube) {
        if (!cube.getPostCalculations().isEmpty()) {
            Dimension dimension = cube.getPostCalculations().get(0).dimension();
            return cube.getDimensions().indexOf(dimension);
        }
        return -1;
    }

}

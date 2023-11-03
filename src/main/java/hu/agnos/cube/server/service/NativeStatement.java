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
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.driver.zolikaokos.DataRetriever;
import hu.agnos.cube.driver.zolikaokos.Problem;
import hu.agnos.cube.meta.queryDto.BaseVectorCoordinateForCube;
import hu.agnos.cube.meta.queryDto.DrillVector;
import hu.agnos.cube.meta.queryDto.DrillVectorForCube;
import hu.agnos.cube.meta.resultDto.ResultElement;
import hu.agnos.cube.meta.resultDto.ResultSet;

public class NativeStatement {

    private final Cube cube;

    public NativeStatement(Cube cube) {
        this.cube = cube;
    }

    public ResultSet[] executeQueries(List<BaseVectorCoordinateForCube> baseVector, List<DrillVectorForCube> drillVectors, List<DrillVector> originalDrills) {
        int numberOfDifferentDrills = originalDrills.size();

        DataRetriever retriever = createDataRetriever(baseVector, drillVectors, numberOfDifferentDrills);
        List<Future<ResultElement>> futures = retriever.computeAll();
        List<List<ResultElement>> tempResult = extractResults(futures, numberOfDifferentDrills);

        ResultSet[] resultSet = new ResultSet[numberOfDifferentDrills];
        for (int i = 0; i < numberOfDifferentDrills; i++) {
            resultSet[i] = new ResultSet(cube.getName(),
                    cube.getDimensionHeader(),
                    Arrays.asList(cube.getMeasures().getHeader()),
                    originalDrills.get(i),
                    drillVectors.get(i).drillRequired(),
                    tempResult.get(i));
        }
        return resultSet;
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
                Logger.getLogger(NativeStatement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tempResult;
    }

    /**
     * Creates a collection of tasks to run to get the results for all the drill request.
     * A task contains only one data retrieval, along a single dimension-coordinate tuple.
     *
     * @param baseVector Base vector the drills are based in, personalized for the cube
     * @param drillVectors The drill vectors personalized for the cube
     * @param numberOfDrills Number of original drills
     * @return The collection of tasks, as a DataRetriever object
     */
    private DataRetriever createDataRetriever(List<BaseVectorCoordinateForCube> baseVector, List<DrillVectorForCube> drillVectors, int numberOfDrills) {
        DataRetriever retriever = new DataRetriever();
        for (int i = 0; i < numberOfDrills; i++) {
            DrillVectorForCube drillVector = drillVectors.get(i);
            Optional<List<List<Node>>> newBaseVectorArray = QueryGenerator.getCoordinatesOfDrill(cube.getDimensions(), baseVector, drillVector);

            if (newBaseVectorArray.isPresent()) {
                for (List<Node> nodes : newBaseVectorArray.get()) {
                    retriever.addProblem(new Problem(cube, i, nodes));
                }
            }
        }
        return retriever;
    }

}

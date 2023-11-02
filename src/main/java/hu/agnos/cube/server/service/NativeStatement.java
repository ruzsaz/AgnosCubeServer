package hu.agnos.cube.server.service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.driver.zolikaokos.DataRetriever;
import hu.agnos.cube.driver.zolikaokos.Problem;
import hu.agnos.cube.meta.drillDto.BaseVectorCoordinateForCube;
import hu.agnos.cube.meta.drillDto.DrillVector;
import hu.agnos.cube.meta.drillDto.DrillVectorForCube;
import hu.agnos.cube.meta.dto.ResultSet;
import hu.agnos.cube.meta.dto.ResultElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author parisek
 */
public class NativeStatement {

    private final Cube cube;

    public NativeStatement(Cube cube) {
        this.cube = cube;
    }

    // TODO: ezt szétszedni
    public ResultSet[] executeQueries(List<BaseVectorCoordinateForCube> baseVector, List<DrillVectorForCube> drillVectors, List<DrillVector> originalDrills) {
        int numberOfDifferentDrills = originalDrills.size();

        DataRetriever retriever = new DataRetriever();

        for (int i = 0; i < numberOfDifferentDrills; i++) {
            DrillVectorForCube drillVector = drillVectors.get(i);
            Optional<List<List<Node>>> newBaseVectorArray = QueryGenerator.getCoordinatesOfDrill(cube.getDimensions(), baseVector, drillVector);

            if (newBaseVectorArray.isPresent()) {
                for (List<Node> nodes : newBaseVectorArray.get()) {
                    retriever.addProblem(new Problem(cube, i, nodes));
                }
            }
        }

        List<Future<ResultElement>> futures = retriever.computeAll();

        List<List<ResultElement>> result = new ArrayList<>();
        for(int i= 0; i < numberOfDifferentDrills; i++){
            List<ResultElement> temp = new ArrayList<>();
            result.add(temp);
        }

        for (Future<ResultElement> future : futures) {
            try {
                ResultElement r = future.get();
                int drillVectorId = r.drillVectorId();
                List<ResultElement> temp = result.get(drillVectorId);
                temp.add(r);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(NativeStatement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // System.out.println("Ennyi kérés van: " + drillVectorsSize + ":" + drillVectors.length + ":" + originalDrills.length + ":" + result.size());

        ResultSet[] resultSet = new ResultSet[numberOfDifferentDrills];
        for(int i = 0; i < numberOfDifferentDrills; i++){
            // System.out.println(i + ": " + cube.getName() + ", " + Arrays.asList(cube.getMeasures().getHeader()) + ", " + drillVectors[i] + ", " + originalDrills[i] + ", res: " + result.get(i).size() + "db.");
            //String cubeName, List<String> measures, List<DrillVector> originalDrills, List<ResultElement> response
            resultSet[i] = new ResultSet(cube.getName(),
                    cube.getDimensionHeader(),
                    Arrays.asList(cube.getMeasures().getHeader()),
                    originalDrills.get(i),
                    drillVectors.get(i).drillRequired(),
                    result.get(i));
        }

        return resultSet;
    }

}

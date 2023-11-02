package hu.agnos.cube.server.service;

import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.driver.util.SetFunctions;
import hu.agnos.cube.meta.drillDto.BaseVectorCoordinateForCube;
import hu.agnos.cube.meta.drillDto.DrillVectorForCube;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author parisek
 */
public class QueryGenerator {

    /**
     * Calculates all the dimension element combinations required to get.
     *
     * @param dimensions Dimensions of the cube
     * @param baseNodeCodes Base drill coordinates, like ["2016,06", ""]
     * @param drillVector 0-1 vector, where 1 shows a drill is required in the given coordinate
     * @return All the required [node, node, ... node] coordinate values folded into an Optional,
     * or an empty Optional if the requested node is missing (= no value for the given coordinate present in the cube)
     */
    public static Optional<List<List<Node>>> getCoordinatesOfDrill(List<Dimension> dimensions, List<BaseVectorCoordinateForCube> baseNodeCodes, DrillVectorForCube drillVector) {
        int coordinateCount = drillVector.drillRequired().length;

        // For each dimension get the required nodes (the base node, or the children, if drill is required).
        List<List<Node>> childrenList = new ArrayList<>();
        for (int i = 0; i < coordinateCount; i++) {
            Node baseNode = dimensions.get(i).getNodeByKnownIdPath(baseNodeCodes.get(i).levelValuesString());

            if (baseNode == null) { // The requested base level coordinate is missing from the cube
                return Optional.empty();
            } else {
                if (drillVector.drillRequired()[i].isRequired()) {
                    childrenList.add(List.of(dimensions.get(i).getChildrenOf(baseNode)));
                } else {
                    childrenList.add(List.of(baseNode));
                }
            }
        }

        List<List<Node>> result = SetFunctions.cartesianProductFromList(childrenList);

        // Only for test
        for (List<Node> nodes : result) {
            // System.out.println(nodes.stream().map(Node::getCode).collect(Collectors.joining(":")));
        }

        return Optional.of(result);
    }


}

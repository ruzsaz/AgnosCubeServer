package hu.agnos.cube.server.service;

import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.driver.util.SetFunctions;
import hu.agnos.cube.extraCalculation.PostCalculation;
import hu.agnos.cube.meta.queryDto.BaseVectorCoordinateForCube;
import hu.agnos.cube.meta.queryDto.DrillVectorForCube;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueryGenerator {

    /**
     * Calculates all the dimension element combinations required to get.
     *
     * @param dimensions Dimensions of the cube
     * @param postCalculations List of postCalculations (like KaplanMeier) in the cube
     * @param baseNodeCodes Base drill coordinates, like ["2016,06", ""]
     * @param drillVector 0-1 vector, where 1 shows a drill is required in the given coordinate
     * @return All the required [node, node, ... node] coordinate values folded into an Optional,
     * or an empty Optional if the requested node is missing (= no value for the given coordinate present in the cube)
     */
    public static Optional<List<List<Node>>> getCoordinatesOfDrill(List<Dimension> dimensions, List<PostCalculation> postCalculations, List<BaseVectorCoordinateForCube> baseNodeCodes, DrillVectorForCube drillVector) {

        int coordinateCount = drillVector.drillRequired().length;

        // For each dimension get the required nodes (the base node, or the children, if drill is required).
        List<List<Node>> childrenList = new ArrayList<>(10);
        for (int i = 0; i < coordinateCount; i++) {
            Node baseNode = dimensions.get(i).getNodeByKnownIdPath(baseNodeCodes.get(i).levelValuesString());
            if (baseNode == null) { // The requested base level coordinate is missing from the cube
                return Optional.empty();
            } else {
                if (drillVector.drillRequired()[i].isRequired() || QueryGenerator.isExtraDrillRequired(postCalculations, dimensions.get(i), baseNode)) {
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

    /**
     * Determines if drill is required in the dimension because a postCalculation is required along it.
     *
     * @param postCalculations List of postCalculations in the cube
     * @param dimension Dimension to consider
     * @param baseNode Base node to initiate the drill from
     * @return True if required, false if not
     */
    private static boolean isExtraDrillRequired(List<PostCalculation> postCalculations, Dimension dimension, Node baseNode) {
        if (baseNode.getChildrenId().length > 0) {
            return postCalculations.stream().anyMatch(p -> p.dimension().getName().equals(dimension.getName()));
        }
        return false;
    }

}

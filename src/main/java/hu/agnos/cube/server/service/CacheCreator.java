package hu.agnos.cube.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.driver.service.DataRetriever;
import hu.agnos.cube.driver.service.Problem;
import hu.agnos.cube.driver.service.ProblemFactory;
import hu.agnos.cube.meta.resultDto.ResultElement;
import hu.agnos.cube.server.repository.CubeRepo;

@Service
public class CacheCreator {

    @Autowired
    CubeRepo cubeRepo;

    private Cube cube;
    private ProblemFactory problemFactory;

    public void createCache(String cubeName) {
        this.cube = cubeRepo.getCubeAndLoadDataIfNotPresent(cubeName);
        this.problemFactory = new ProblemFactory(cube);
        cube.setCache(new HashMap<>(10));
        List<Node> topNode = cube.getDimensions().stream().map(dimension -> dimension.getNodes()[0][0]).toList();
        addToCacheIfComplexityHihgerThan(topNode, 100000);
    }

    private void addToCacheIfComplexityHihgerThan(List<Node> baseVector, int complexity) {
        Problem problem = problemFactory.createProblem(baseVector);
        int affectedRows = problem.getNumberOfAffectedRows();
        System.out.println("Checking complexity of " + affectedRows + ": " + baseVector);
        if (affectedRows > complexity) {
            System.out.println("Adding to cache with complexity of " + affectedRows + ": " + baseVector);
            DataRetriever retriever = new DataRetriever();
            retriever.addProblem(problem);
            List<Future<ResultElement>> futures = retriever.computeAll();
            List<ResultElement> tempResult = CubeService.extractResults(futures);
            cube.getCache().put(baseVector, tempResult.get(0).measureValues());
            getChildQueries(baseVector).forEach(childQuery -> addToCacheIfComplexityHihgerThan(childQuery, complexity));
        }
    }

    private List<List<Node>> getChildQueries(List<Node> baseVector) {
        int dimSize = baseVector.size();
        List<List<Node>> childrenList = new ArrayList<>(10);
        for (int dimToDrillIn = 0; dimToDrillIn < dimSize; dimToDrillIn++) {
            List<Node> childrenInDrillCoordinate = List.of(cube.getDimensions().get(dimToDrillIn).getChildrenOf(baseVector.get(dimToDrillIn)));
            for (Node childInDrillCoordinate : childrenInDrillCoordinate) {
                List<Node> child = new ArrayList<>(dimSize);
                for (int i = 0; i < dimSize; i++) {
                    if (i == dimToDrillIn) {
                        child.add(childInDrillCoordinate);
                    } else {
                        child.add(baseVector.get(i));
                    }
                }
                childrenList.add(child);
            }
        }
        return childrenList;
    }

}

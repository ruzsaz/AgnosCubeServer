package hu.agnos.cube.server.controller;

import hu.agnos.cube.meta.queryDto.CubeQuery;
import hu.agnos.cube.meta.resultDto.CubeList;
import hu.agnos.cube.meta.resultDto.ResultSet;
import hu.agnos.cube.server.service.CubeListService;
import hu.agnos.cube.server.service.CubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Controller to handle the data cube requests: loading the cube's data, querying the data,
 * and querying the available cubes.
 */
@RestController
public class CubeController {

    private static final Logger log = LoggerFactory.getLogger(CubeController.class);

    @Autowired
    private CubeListService cubeListService;

    @Autowired
    private CubeService cubeService;

    /**
     * Prepares the data cubes for the given cube names: loads the data if not present.
     *
     * @param cubeNames List of cube names to prepare
     * @return "OK" if the preparation was successful
     */
    @PostMapping(value = "/prepare", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> prepareCubes(@RequestBody List<String> cubeNames) {
        long start = System.currentTimeMillis();
        log.info("Starting to prepare cubes {}" , cubeNames);
        cubeService.prepareCubes(cubeNames);
        Optional<String> result = Optional.of("OK");
        ResponseEntity<?> answer = result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end = System.currentTimeMillis();
        log.info("Pre-loading the data cubes took {}ms", end - start);
        return answer;
    }

    /**
     * Gets the data for the given queries.
     *
     * @param queries List of queries to resolve
     * @return The answers for the queries
     */
    @PostMapping(value = "/data", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> getData(@RequestBody List<CubeQuery> queries) {
        long start = System.currentTimeMillis();
        Optional<List<ResultSet>> result = Optional.ofNullable(cubeService.getData(queries));
        ResponseEntity<?> answer = result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end = System.currentTimeMillis();
        log.info("Data access in {}ms", end - start);
        return answer;
    }

    /**
     * Gets the list of available cubes.
     *
     * @return The list of available cubes
     */
    @GetMapping("/cube_list")
    ResponseEntity<?> getCubeList() {
        CubeList cubeNameAndList = cubeListService.getCubeList();
        Optional<CubeList> result = Optional.ofNullable(cubeNameAndList);
        if(result.isPresent()) {
            log.info("Sending cube list of {} cubes", cubeNameAndList.cubeMap().size());
        }
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}

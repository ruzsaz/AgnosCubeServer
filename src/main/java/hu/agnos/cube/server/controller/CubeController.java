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

@RestController
public class CubeController {

    private final Logger log = LoggerFactory.getLogger(CubeController.class);

    @Autowired
    private CubeListService cubeListService;

    @Autowired
    private CubeService cubeService;

    @PostMapping(value = "/data", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> getData(@RequestBody List<CubeQuery> queries) {
        long start = System.currentTimeMillis();
        Optional<List<ResultSet>> result = Optional.ofNullable(cubeService.getData(queries));
        ResponseEntity<?> answer = result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end = System.currentTimeMillis();
        log.info("Data access in " + (end-start) + " ms");
        return answer;
    }

    @GetMapping("/cube_list")
    ResponseEntity<?> getCubeList() {
        CubeList cubeNameAndList = cubeListService.getCubeList();
        Optional<CubeList> result = Optional.ofNullable(cubeNameAndList);
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}

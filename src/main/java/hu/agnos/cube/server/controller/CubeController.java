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

import java.util.Optional;

@RestController
public class CubeController {

    private final Logger log = LoggerFactory.getLogger(CubeController.class);

    @Autowired
    private CubeListService cubeListService;

    @Autowired
    private CubeService cubeService;

    @PostMapping(value = "/data", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> getData(@RequestBody CubeQuery query) {

        long start3 = System.currentTimeMillis();
        Optional<ResultSet[]> result3 = Optional.ofNullable(cubeService.getData(query, 3));
        ResponseEntity<?> answ3 = result3.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end3 = System.currentTimeMillis();

        long start2 = System.currentTimeMillis();
        Optional<ResultSet[]> result2 = Optional.ofNullable(cubeService.getData(query, 2));
        ResponseEntity<?> answ2 = result2.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end2 = System.currentTimeMillis();

        long start4 = System.currentTimeMillis();
        Optional<ResultSet[]> result4 = Optional.ofNullable(cubeService.getData(query, 1));
        ResponseEntity<?> answ4 = result4.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end4 = System.currentTimeMillis();

        long start5 = System.currentTimeMillis();
        Optional<ResultSet[]> result5 = Optional.ofNullable(cubeService.getData(query, 3));
        ResponseEntity<?> answ5 = result5.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end5 = System.currentTimeMillis();

        long start6 = System.currentTimeMillis();
        Optional<ResultSet[]> result6 = Optional.ofNullable(cubeService.getData(query, 2));
        ResponseEntity<?> answ6 = result6.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end6 = System.currentTimeMillis();

        long start1 = System.currentTimeMillis();
        Optional<ResultSet[]> result1 = Optional.ofNullable(cubeService.getData(query, 1));
        ResponseEntity<?> answ1 = result1.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        long end1 = System.currentTimeMillis();


        System.out.println("Nyuszi: " + (end1-start1) + " ms");
        System.out.println("Kecske: " + (end6-start6) + " ms");
        System.out.println("Kecske+: " + (end3-start3) + " ms");


        return answ3;
    }

    @GetMapping("/cube_list")
    ResponseEntity<?> getCubeList() {
        CubeList cubeNameAndList = cubeListService.getCubeList();
        Optional<CubeList> result = Optional.ofNullable(cubeNameAndList);
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}

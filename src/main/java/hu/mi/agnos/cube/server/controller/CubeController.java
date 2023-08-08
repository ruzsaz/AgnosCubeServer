/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.mi.agnos.cube.server.controller;

import hu.agnos.cube.meta.dto.CubeList;
import hu.agnos.cube.driver.ResultSet;
import hu.agnos.cube.meta.dto.HierarchyDTO;
import hu.mi.agnos.cube.server.service.CubeService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author parisek
 */
@RestController
//@RequestMapping("/cube")
public class CubeController {

    private final Logger log = LoggerFactory.getLogger(CubeController.class);

    @Autowired
    private CubeService cubeService;

    @GetMapping("/data")
    ResponseEntity<?> getData(@RequestParam(value = "name", required = true) String cubeName,
            @RequestParam(value = "base", required = true) String baseVector,
            @RequestParam(value = "drill", required = true) String drillVectors) {
        Optional<ResultSet[]> result = Optional.ofNullable(cubeService.getData(cubeName, baseVector, drillVectors));
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/hierarchy")
    ResponseEntity<?> getHierarchyByName(@RequestParam(value = "cubename", required = true) String cubeName,
            @RequestParam(value = "hierarchyname", required = true) String hierarchyName) {
        Optional<HierarchyDTO> result = Optional.ofNullable(cubeService.getHierarchy(cubeName, hierarchyName));
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/cube_list")
    ResponseEntity<?> getCubeList() {
        CubeList cubeNameAndList = cubeService.getCubesDate();
        Optional<CubeList> result = Optional.ofNullable(cubeNameAndList);
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/measure_header")
    ResponseEntity<?> getMeasureHeader(@RequestParam(value = "name", required = true) String cubeName) {
        Optional<String[]> result = Optional.ofNullable(cubeService.getMeasureHeader(cubeName));
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/hierarchy_header")
    ResponseEntity<?> getHierarchyHeader(@RequestParam(value = "name", required = true) String cubeName) {
        Optional<String[]> result = Optional.ofNullable(cubeService.getHierarchyHeader(cubeName));
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

   

}

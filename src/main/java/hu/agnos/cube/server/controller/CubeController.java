package hu.agnos.cube.server.controller;

import java.util.Optional;

import hu.agnos.cube.meta.drillDto.CubeQuery;
import hu.agnos.cube.meta.dto.CubeList;
import hu.agnos.cube.meta.dto.ResultSet;
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

/**
 *
 * @author parisek
 */
@RestController
public class CubeController {

    private final Logger log = LoggerFactory.getLogger(CubeController.class);

    @Autowired
    private CubeService cubeService;

    @PostMapping(value = "/data", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> getData(@RequestBody CubeQuery query) {
        Optional<ResultSet[]> result = Optional.ofNullable(cubeService.getData(query));
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/cube_list")
    ResponseEntity<?> getCubeList() {
        CubeList cubeNameAndList = cubeService.getCubeList();
        Optional<CubeList> result = Optional.ofNullable(cubeNameAndList);
        return result.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

//    @GetMapping("/dimension_header")
//    ResponseEntity<?> getDimensionHeader(@RequestParam(value = "name") String cubeName) {
//        Optional<String[]> result = Optional.ofNullable(cubeService.getDimensionHeader(cubeName));
//        return result.map(response -> ResponseEntity.ok().body(response))
//                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }

}

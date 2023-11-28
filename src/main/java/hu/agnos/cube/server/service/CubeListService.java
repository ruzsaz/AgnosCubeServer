package hu.agnos.cube.server.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.agnos.cube.Cube;
import hu.agnos.cube.meta.queryDto.CubeQuery;
import hu.agnos.cube.meta.resultDto.CubeList;
import hu.agnos.cube.meta.resultDto.CubeMetaDTO;
import hu.agnos.cube.meta.resultDto.DimensionDTO;
import hu.agnos.cube.server.repository.CubeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author parisek
 */
@Service
public class CubeListService {

    @Autowired
    CubeRepo cubeRepo;

    public CubeList getCubeList() {
        Map<String, CubeMetaDTO> cubeMap = new HashMap<>(16);
        for (String cubeName : cubeRepo.keySet()) {
            Cube cube = cubeRepo.getCube(cubeName);
            Date agnosCreatedDate = cube.getCreatedDate();
            List<DimensionDTO> dimensionHeader = cube.getDimensions().stream().map(DimensionDTO::fromDimension).toList();
            String[] measureHeader = cube.getMeasureHeader();

            CubeMetaDTO cubeMetaDTO = new CubeMetaDTO(agnosCreatedDate, cube.getHash(), dimensionHeader, measureHeader);
            cubeMap.put(cubeName, cubeMetaDTO);
        }
        return new CubeList(cubeMap);
    }

}

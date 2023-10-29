package hu.agnos.cube.server.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.agnos.cube.Cube;
import hu.agnos.cube.meta.drillDto.CubeQuery;
import hu.agnos.cube.meta.dto.CubeList;
import hu.agnos.cube.meta.dto.CubeMetaDTO;
import hu.agnos.cube.meta.dto.DimensionDTO;
import hu.agnos.cube.meta.dto.ResultSet;
import hu.agnos.cube.server.repository.CubeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author parisek
 */
@Service
public class CubeService {

    @Autowired
    CubeRepo cubeRepo;

    public CubeList getCubeList() {
        Map<String, CubeMetaDTO> cubeMap = new HashMap<>();
        for (String cubeName : cubeRepo.keySet()) {
            Cube c = cubeRepo.getCube(cubeName);

            Date agnosCreatedDate = c.getCreatedDate();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String createdDateString = dateFormat.format(agnosCreatedDate);

            List<DimensionDTO> dimensionHeader = c.getDimensions().stream().map(DimensionDTO::fromDimension).toList();
            String[] measureHeader = c.getMeasureHeader();

            CubeMetaDTO cubeMetaDTO = new CubeMetaDTO(agnosCreatedDate, dimensionHeader, measureHeader);
            cubeMap.put(cubeName, cubeMetaDTO);
        }
        return new CubeList(cubeMap);
    }

    public ResultSet[] getData(CubeQuery query) {
        Cube cube = cubeRepo.getCube(query.cubeName());
        NativeStatement statement = new NativeStatement(cube);
        return statement.executeQueries(query.baseVector(), query.drillVectors(), query.originalDrills());
    }

    public String[] getDimensionHeader(String cubeName) {
        Cube cube = cubeRepo.getCube(cubeName);
        return cube.getDimensionHeader();
    }

    public String[] getMeasureHeader(String cubeName) {
        Cube cube = cubeRepo.getCube(cubeName);
        return cube.getMeasureHeader();
    }
}

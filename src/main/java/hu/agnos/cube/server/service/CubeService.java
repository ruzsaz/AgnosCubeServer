/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.agnos.cube.server.service;

import hu.agnos.cube.driver.NativeStatement;
import hu.agnos.cube.driver.ResultSet;
import hu.agnos.cube.meta.dto.CubeNameAndDate;
import hu.agnos.cube.meta.dto.CubeList;
import hu.agnos.cube.meta.dto.HierarchyDTO;
import hu.agnos.cube.server.repository.CubeRepo;
import hu.agnos.molap.Cube;
import hu.agnos.molap.dimension.Hierarchy;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public CubeList getCubesDate() {
        CubeList result = new CubeList();
        for (String cubeName : cubeRepo.keySet()) {
            String createdDateString = "";
            Cube c = cubeRepo.getCube(cubeName);
            Date agnosCreatedDate = c.getCreatedDate();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            createdDateString = dateformat.format(agnosCreatedDate);
            CubeNameAndDate cubeNameAndDate = new CubeNameAndDate(cubeName, createdDateString);
            result.getCubesNameAndDate().add(cubeNameAndDate);
        }
        return result;
    }

    public ResultSet[] getData(String cubeName, String baseVector, String drillVectors) {
        ResultSet[] result = null;
        Cube cube = cubeRepo.getCube(cubeName);
        NativeStatement statement = new NativeStatement(cube);
        result = statement.executeQueries(baseVector, drillVectors.split(","));
        return result;
    }

    public String[] getHierarchyHeader(String cubeName) {
        Cube cube = cubeRepo.getCube(cubeName);
        return cube.getHierarchyHeader();        
    }
    
    public HierarchyDTO getHierarchy(String cubeName, String hierarchyName) {       
        Cube cube = cubeRepo.getCube(cubeName);
        
        int dimIndex = cube.getHierarchyInfoByHeader(hierarchyName)[0];
        int hierIndex = cube.getHierarchyInfoByHeader(hierarchyName)[1];
        Hierarchy hierarchy =  cube.getDimensions().get(dimIndex).getHierarchies()[hierIndex];
                
        return new HierarchyDTO(hierarchy);        
    }
        public String[] getMeasureHeader(String cubeName) {
        Cube cube = cubeRepo.getCube(cubeName);
        return cube.getMeasureHeader();
                
    }
}

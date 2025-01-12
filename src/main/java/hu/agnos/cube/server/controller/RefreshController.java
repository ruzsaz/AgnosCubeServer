package hu.agnos.cube.server.controller;

import hu.agnos.cube.server.entity.RefreshInfoHolder;
import hu.agnos.cube.server.repository.CubeRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to refresh/restart the cube repository.
 */
@RestController
public class RefreshController {

    private static final Logger log = LoggerFactory.getLogger(RefreshController.class);

    @Autowired
    CubeRepo cubeRepo;
    @Autowired
    private RefreshInfoHolder refreshInfoHolder;

    /**
     * Refreshes the cube repository by reloading the changed/new cubes from the cube directory.
     *
     * @return "Refreshed" if the refresh was successful
     */
    @PostMapping("/refresh")
    public String refresh() {
        long start = System.currentTimeMillis();
        refreshInfoHolder.setRefreshInProgress(true);
        cubeRepo.refreshFromCubeDirectory();
        refreshInfoHolder.setRefreshInProgress(false);
        long end = System.currentTimeMillis();
        log.info("Cube repository refreshed in {}ms", end - start);
        return "Refreshed";
    }

    /**
     * Restarts the cube repository by clearing it and reloading all the cubes from the cube directory.
     *
     * @return "Restarted" if the restart was successful
     */
    @PostMapping("/restart")
    public String restart() {
        long start = System.currentTimeMillis();
        refreshInfoHolder.setRefreshInProgress(true);
        cubeRepo.clear();
        cubeRepo.refreshFromCubeDirectory();
        refreshInfoHolder.setRefreshInProgress(false);
        long end = System.currentTimeMillis();
        log.info("Cube repository RESTARTED in {}ms", end - start);
        return "Restarted";
    }

}

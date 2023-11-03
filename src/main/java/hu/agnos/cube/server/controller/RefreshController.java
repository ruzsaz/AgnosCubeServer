package hu.agnos.cube.server.controller;

import hu.agnos.cube.server.entity.RefreshInfoHolder;
import hu.agnos.cube.server.repository.CubeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefreshController {

    @Autowired
    CubeRepo cubeRepo;
    @Autowired
    private RefreshInfoHolder refreshInfoHolder;

    @PostMapping("/refresh")
    public String refresh() {
        refreshInfoHolder.setRefreshInProgress(true);
        cubeRepo.refresh();
        refreshInfoHolder.setLastRefreshTimeInMilliseconds(System.currentTimeMillis());
        refreshInfoHolder.setRefreshInProgress(false);
        return "Refreshed";
    }

}

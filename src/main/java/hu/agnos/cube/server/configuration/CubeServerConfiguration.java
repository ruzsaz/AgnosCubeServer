package hu.agnos.cube.server.configuration;

import hu.agnos.cube.server.repository.CubeRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class CubeServerConfiguration {
    @Bean
    public static CubeRepo getCubeRepo() {
        CubeRepo cubeRepo = new CubeRepo();
        cubeRepo.refreshFromCubeDirectory();
        return cubeRepo;
    }

}

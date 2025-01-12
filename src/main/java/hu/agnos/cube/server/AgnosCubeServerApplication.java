/**
 * Web service that listens to data requests (POST requests) from Agnos' ReportServer. A request consist of a Cube, a
 * BaseVector and a list of DrillVectors. The CubeServer determines the answers for the drills in the requests, applies
 * the necessary post-calculations (like Kaplan-Meier estimation), and answers the request by sending the requested data
 * in the response-body.
 */
package hu.agnos.cube.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the CubeServer application.
 */
@SpringBootApplication
public class AgnosCubeServerApplication {

    /**
     * Starts the CubeServer application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AgnosCubeServerApplication.class, args);
    }

}

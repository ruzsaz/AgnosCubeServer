/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.mi.agnos.cube.server.repository;

import hu.agnos.molap.Cube;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CubeRepo extends HashMap<String, Cube> {

    public CubeRepo() {
        super();
    }

    public static CubeRepo loader() {
        CubeRepo tempInstance = new CubeRepo();

        String path = null;
        final String CUBES_DIR = System.getenv("CUBES_DIR");
        final String AGNOS_HOME = System.getenv("AGNOS_HOME");

        final File folder = (CUBES_DIR != null) ? new File(CUBES_DIR) : new File(AGNOS_HOME, "/AgnosReportingServer/Cubes");
        System.out.println("Cube base dir: " + folder.getAbsolutePath());
        for (final File fileEntry : folder.listFiles()) {
            String fileName = fileEntry.getName();
            if (fileName.toLowerCase().endsWith(".cube")) {

                Cube cube = null;
                try {
                    FileInputStream fileIn = new FileInputStream(fileEntry);
                    //FileInputStream fileIn = new FileInputStream(path + "/" + fileName);

                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    System.out.println("betöltés alatt: " + fileName);
                    cube = (Cube) in.readObject();

                    in.close();
                    fileIn.close();
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(CubeRepo.class.getName()).log(Level.SEVERE, "MOLAP Cube loading failed.", ex);

                }
                if (cube != null) {
                    tempInstance.put(cube.getName(), cube);
                }

            }
        }
        return tempInstance;
    }

    public Cube getCube(String cubeUniqueName) {
        return this.get(cubeUniqueName);
    }

}

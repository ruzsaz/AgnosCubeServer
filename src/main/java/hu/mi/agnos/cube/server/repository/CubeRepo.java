/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.mi.agnos.cube.server.repository;

import hu.agnos.molap.Cube;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class CubeRepo extends HashMap<String, Cube> {
    
    
    

    private static CubeRepo instance = null;

    public static synchronized CubeRepo getInstance() {
        if (instance == null) {
            instance = new CubeRepo().loader();
        }
        return instance;
    }

    public CubeRepo() {
        super();
    }

    public void refresh() {
        instance = loader();
    }

    private CubeRepo loader() {
        CubeRepo tempInstance = new CubeRepo();

        String path = null;
        final String AGNOS_HOME = System.getenv("AGNOS_HOME");
        if (!AGNOS_HOME.endsWith("/")) {
            path = AGNOS_HOME + "/AgnosReportingServer/";
        } else {
            path = AGNOS_HOME + "AgnosReportingServer/";
        }
        final File folder = new File(path + "Cubes");
        for (final File fileEntry : folder.listFiles()) {
            String fileName = fileEntry.getName();
            if (fileName.toLowerCase().endsWith(".cube")) {

                Cube cube = null;
                try {
                    FileInputStream fileIn = new FileInputStream(path + "Cubes/" + fileName);
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

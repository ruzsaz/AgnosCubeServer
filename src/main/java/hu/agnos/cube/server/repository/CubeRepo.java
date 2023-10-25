/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.server.repository;

import hu.agnos.molap.Cube;

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

        final String CUBES_DIR = System.getenv("AGNOS_CUBES_DIR");

        final File folder = new File(CUBES_DIR);
        System.out.println("Cube base dir: " + folder.getAbsolutePath());
        for (final File fileEntry : folder.listFiles()) {
            String fileName = fileEntry.getName();
            if (fileName.toLowerCase().endsWith(".cube")) {

                Cube cube = null;
                try {
                    FileInputStream fileIn = new FileInputStream(fileEntry);

                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    System.out.println("loading " + fileName + "...");
                    cube = (Cube) in.readObject();

                    in.close();
                    fileIn.close();
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(CubeRepo.class.getName()).log(Level.SEVERE, "MOLAP Cube loading failed.", ex);

                }
                if (cube != null) {
                    // cube.init();
                    tempInstance.put(cube.getName(), cube);
                }

            }
        }
        return tempInstance;
    }

    public void refresh() {
        this.clear();
        CubeRepo tmp = CubeRepo.loader();
        this.putAll(tmp);
    }

    public Cube getCube(String cubeUniqueName) {
        return this.get(cubeUniqueName);
    }

}

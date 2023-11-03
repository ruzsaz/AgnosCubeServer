package hu.agnos.cube.server.repository;

import hu.agnos.cube.Cube;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CubeRepo extends HashMap<String, Cube> {

    public CubeRepo() {
        super();
    }

    public static CubeRepo load() {
        CubeRepo cubeRepo = new CubeRepo();

        String CUBES_DIR = System.getenv("AGNOS_CUBES_DIR");

        File folder = new File(CUBES_DIR);
        System.out.println("Cube base dir: " + folder.getAbsolutePath());
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            String fileName = fileEntry.getName();
            if (fileName.toLowerCase().endsWith(".cube")) {

                try (FileInputStream fileIn = new FileInputStream(fileEntry); ObjectInputStream in = new ObjectInputStream(fileIn)) {
                    System.out.println("loading " + fileName + "...");
                    Cube cube = (Cube) in.readObject();
                    cube.init();
                    cubeRepo.put(cube.getName(), cube);
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(CubeRepo.class.getName()).log(Level.SEVERE, "Cube loading failed.", ex);
                }

            }
        }
        return cubeRepo;
    }

    public void refresh() {
        clear();
        putAll(load());
    }

    public Cube getCube(String cubeName) {
        return get(cubeName);
    }

}

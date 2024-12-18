package hu.agnos.cube.server.repository;

import hu.agnos.cube.ClassicalCube;
import hu.agnos.cube.CountDistinctCube;
import hu.agnos.cube.Cube;
import hu.agnos.cube.measure.Measure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;
import org.slf4j.LoggerFactory;

public class CubeRepo extends HashMap<String, Cube> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CubeRepo.class);

    @Getter
    private File cubesFolder = null;

    public CubeRepo() {
        super();
        String cubesDir = System.getenv("AGNOS_CUBES_DIR");
        cubesFolder = new File(cubesDir);
        log.debug("Cube base dir: " + cubesFolder.getAbsolutePath());
    }

    public synchronized void refreshFromCubeDirectory() {
        Map<String, Cube> newCubeMap = new HashMap<>();
        for (File file : Objects.requireNonNull(cubesFolder.listFiles())) {
            Optional<Cube> cube = findOrLoad(file);
            cube.ifPresent(c ->newCubeMap.put(c.getName(), c));
        }
        clear();
        putAll(newCubeMap);
    }

    private Optional<Cube> findOrLoad(File file) {
        String fileName = file.getName();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".cube")) {
            log.debug(fileName + " found but not a cube, ignored.");
            return Optional.empty();
        }
        String hash = CubeRepo.getHash(file);
        Optional<Cube> oldCube = values().stream().filter(cube -> cube.getHash().equals(hash)).findFirst();
        if (oldCube.isPresent()) {
            log.debug(oldCube.get().getName() + " is already loaded, skipping.");
            return oldCube;
        }

        return CubeRepo.loadFileAsCube(file);
    }

    private static Optional<Cube> loadFileAsCube(File file) {
        Cube result = null;
        String fileHash = CubeRepo.getHash(file);
        try (FileInputStream fileIn = new FileInputStream(file); ObjectInput in = new ObjectInputStream(fileIn)) {
            log.info("Loading " + file.getName() + " as new cube.");
            Object o = in.readObject();
            if (o.getClass().equals(CountDistinctCube.class )){
                CountDistinctCube cube = (CountDistinctCube) o;
                cube.initCountDistinctCube();
                cube.setHash(fileHash);
                result = cube;
            }
            else {
                ClassicalCube cube = (ClassicalCube) o;
                cube.setHash(fileHash);
                result = cube;
            }
//            if (file.getName().equals("AmiTuleles.cube")) {
//                FileOutputStream fileOut = new FileOutputStream(file.getName() + "2");
//                ObjectOutputStream out = new ObjectOutputStream(fileOut);
//                //result.setName("CrcPrevalencia");
//                ((Measure) result.getMeasureByName("POPULATION_SIZE")).setHidden(false);
//                out.writeObject(result);
//                out.close();
//                fileOut.close();
//                System.out.println("Serialized data is saved ");
//            }


        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(CubeRepo.class.getName()).log(Level.SEVERE, "Cube loading failed.", ex);
        }
        return Optional.ofNullable(result);
    }

    private static String getHash(File file) {
        String hash = "";
        try {
            BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
            hash = file.getPath() + attr.size() + attr.lastModifiedTime().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return hash;
    }

    public Cube getCube(String cubeName) {
        return get(cubeName);
    }

    public void putCube(Cube cube) {
        put(cube.getName(), cube);
    }

    private Optional<Cube> getByHash(String hash) {
        return values().stream().filter(cube -> cube.getHash().equals(hash)).findFirst();
    }

}

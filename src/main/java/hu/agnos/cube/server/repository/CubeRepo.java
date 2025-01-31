package hu.agnos.cube.server.repository;

import hu.agnos.cube.CountDistinctCube;
import hu.agnos.cube.Cube;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Synchronized;
import org.slf4j.LoggerFactory;

/**
 * Repository for cubes. It loads cubes from the cube directory, and keeps them in memory. It evicts
 * the oldest cubes if the memory usage is above the limit.
 */
public class CubeRepo {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CubeRepo.class);
    private final Map<String, Cube> cubesMap = new ConcurrentHashMap<>(20);
    private long maxMemoryInBytes = 10 * 1000000000L; // Environment variable will override this, if found
    private static final long EVICTONLYOLDERTHANDURATION = 200L; // 0.2 seconds

    @Getter
    private File cubesFolder;

    /**
     * Constructor for the CubeRepo. It sets the cube directory from the environment variable.
     */
    public CubeRepo() {
        String cubesDir = System.getenv("AGNOS_CUBES_DIR");
        String cubesMemoryLimit = System.getenv("CUBES_MEMORY_LIMIT");
        if (cubesMemoryLimit != null) {
            maxMemoryInBytes = 1000000000L * Long.parseLong(cubesMemoryLimit);
        }
        if (cubesDir == null) {
            log.error("AGNOS_CUBES_DIR environment variable is not set, cannot start CubeRepo.");
            return;
        }
        cubesFolder = new File(cubesDir);
        log.info("CubeRepo started, with base dir: {}. Max. memory allowed: {} MB.", cubesFolder.getAbsolutePath(), maxMemoryInBytes / 1000000);
    }

    /**
     * Refresh the cube repo from the cube directory. It reads all the cube files from the directory
     * and loads them changed, or not already loaded. Adds all of them to the repo, and evicts the
     * oldest cubes if the memory usage is above the limit.
     */
    public synchronized void refreshFromCubeDirectory() {
        File[] files = Objects.requireNonNull(cubesFolder.listFiles());
        Stream.of(files).forEach(file -> {
            Optional<Cube> cube = findOrLoad(file);
            cube.ifPresent(this::putCube);
            forceMemoryPolicy(0, 0);
        });
    }

    /**
     * Force the memory policy to free up memory if the memory usage is above the limit, and the
     * additional file length is added to the memory usage.
     *
     * @param olderThanMillis Only consider cubes that have not been accessed for at least this many milliseconds
     * @param additionalFileLength Additional file length to add to the memory usage
     */
    private void forceMemoryPolicy(long olderThanMillis, long additionalFileLength) {
        long currentMemoryUsage = getCurrentMemoryUsage();
        while (currentMemoryUsage + additionalFileLength > maxMemoryInBytes && currentMemoryUsage > 0) {
            Optional<Cube> oldestCube = getOldestAccessedCube(olderThanMillis);
            if (oldestCube.isPresent()) {
                oldestCube.get().dropCells();
                currentMemoryUsage = getCurrentMemoryUsage();
                log.info("Need {} MB free memory, so cube {} evicted. Free: {} MB",
                        additionalFileLength / 1000000,
                        oldestCube.get().getName(),
                        (maxMemoryInBytes - currentMemoryUsage) / 1000000);
            } else {
                log.error("Failed to drop cubes to meet the memory requirements. Free: {} MB", (maxMemoryInBytes - currentMemoryUsage - additionalFileLength) / 1000000);
                break;
            }
        }
    }

    private Optional<Cube> getOldestAccessedCube(long olderThanMillis) {
        long maxAccessTime = System.currentTimeMillis() - olderThanMillis;
        return cubesMap.values().stream().filter(cube -> cube.getLastAccessTime() < maxAccessTime).min(Comparator.comparingLong(Cube::getLastAccessTime));
    }

    /**
     * Find an already loaded cube in the repo, or load a cube from a file if it is not present.
     *
     * @param file File to load
     * @return Cube object
     */
    private Optional<Cube> findOrLoad(File file) {
        String fileName = file.getName();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".cube")) {
            log.debug("{} found, but not a cube, ignored.", fileName);
            return Optional.empty();
        }
        String hash = CubeRepo.getHash(file);
        Optional<Cube> oldCube = cubesMap.values().stream().filter(cube -> cube.getHash().equals(hash)).findFirst();
        if (oldCube.isPresent()) {
            log.debug("{} is already loaded, skipping.", oldCube.get().getName());
            return oldCube;
        }
        forceMemoryPolicy(0, file.length());
        return CubeRepo.loadFileAsCube(file);
    }

    /**
     * Load a cube from a file.
     *
     * @param file File to load
     * @return Cube object
     */
    private static Optional<Cube> loadFileAsCube(File file) {
        Cube result = null;
        String fileHash = CubeRepo.getHash(file);
        long fileSize = file.length();
        try (FileInputStream fileIn = new FileInputStream(file); ObjectInput in = new ObjectInputStream(fileIn)) {
            Object o = in.readObject();
            if (o.getClass().equals(CountDistinctCube.class )){
                CountDistinctCube cube = (CountDistinctCube) o;
                cube.initCountDistinctCube();
                result = cube;
            }
            else {
                result = (Cube) o;
            }
            result.setHash(fileHash);
            result.setFileSize(fileSize);
            result.setLastAccessTime(System.currentTimeMillis());
            log.info("Cube data loaded from file: {} ({} MB)", file.getName(), fileSize / 1000000);

        } catch (IOException | ClassNotFoundException ex) {
            log.error("Cube loading failed from file: {}", file.getName(), ex);
        }
        return Optional.ofNullable(result);
    }

    /**
     * Calculate a hash for a file, based on its path, size and last modification time.
     *
     * @param file File to calculate hash for
     * @return Hash of the file
     */
    private static String getHash(File file) {
        String hash = "";
        try {
            BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
            hash = file.getPath() + attr.size() + attr.lastModifiedTime().toString();
        } catch (IOException ex) {
            log.error("Failed to calculate hash of cube file: {}", file.getName(), ex);
        }
        return hash;
    }

    /**
     * Get a cube from the repo, and load it from the disk if it is not present.
     *
     * @param cubeName Name of the cube
     * @return Cube object
     */
    public Cube getCubeAndLoadDataIfNotPresent(String cubeName) {
        Cube cube = cubesMap.get(cubeName);
        synchronized (cube) {
            if (!cube.isDataPresent()) {
                File file = new File(cubesFolder, cubeName + ".cube");
                forceMemoryPolicy(EVICTONLYOLDERTHANDURATION, file.length());
                Optional<Cube> newCube = CubeRepo.loadFileAsCube(new File(cubesFolder, cubeName + ".cube"));
                if (newCube.isPresent()) {
                    cube = newCube.get();
                    cube.setLastAccessTime(System.currentTimeMillis());
                    putCube(cube);
                    forceMemoryPolicy(EVICTONLYOLDERTHANDURATION, 0);
                }
            }
        }
        return cube;
    }

    /**
     * Get a cube from the repo. It is possible that the cube is not loaded into memory.
     *
     * @param cubeName Name of the cube
     * @return Cube object
     */
    public Cube getCube(String cubeName) {
        return cubesMap.get(cubeName);
    }

    private void putCube(Cube cube) {
        cubesMap.put(cube.getName(), cube);
    }

    /**
     * Clear the cube repo.
     */
    public void clear() {
        cubesMap.clear();
    }

    /**
     * Get the key set of the cube repo.
     *
     * @return Key set of the cube repo
     */
    public Collection<String> keySet() {
        return cubesMap.keySet();
    }

    /**
     * Get the current memory usage of the cube repo in bytes.
     *
     * @return Memory usage in bytes
     */
    private long getCurrentMemoryUsage() {
        return cubesMap.keySet().stream().map(cubesMap::get).mapToLong(Cube::getFileSize).sum();
    }

}

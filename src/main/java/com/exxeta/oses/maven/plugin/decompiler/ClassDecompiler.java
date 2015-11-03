/**
 * Copyright (C) 2015 Benjamin Asbach (benjamin.asbach@exxeta.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exxeta.oses.maven.plugin.decompiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:benjamin.asbach@exxeta.com">Benjamin Asbach, 2015</a>
 */
public abstract class ClassDecompiler implements JarDecompiler {

    private final Logger logger = LoggerFactory.getLogger(ClassDecompiler.class);
    
    @Override
    public DecompilationStatistics decompileJar(Path jarToDecompile, Path destination) throws IOException {
        if (Files.exists(destination)) {
            throw new FileAlreadyExistsException(destination.toString());
        }
        logger.debug("Decompiling {}", jarToDecompile);

        FileSystem jarFileSystem = createJarFileSystem(destination);
        return copyEntriesFromSourceZipToDestination(jarToDecompile, jarFileSystem);
    }
    
    private FileSystem createJarFileSystem(Path jar) throws IOException {
        URI zipUri = URI.create("jar:file:" + jar.toUri().getPath());
        Map<String, Object> env = new HashMap<>();
        env.put("create", "true");
        return FileSystems.newFileSystem(zipUri, env);
    }
    
    private DecompilationStatistics copyEntriesFromSourceZipToDestination(Path jarToDecompile, final FileSystem destinationZipFileSystem) throws IOException {
        final DecompilationStatistics stats = new DecompilationStatistics();

        FileSystem sourceJarFileSystem = createJarFileSystem(jarToDecompile);
        Files.walkFileTree(sourceJarFileSystem.getPath("/"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path dirInZip = destinationZipFileSystem.getPath(dir.toString());
                if (!Files.exists(dirInZip)) {
                    Files.createDirectory(dirInZip);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (fileIsAClass(file)) {
                    logger.debug("Decompiling class {}", file.getFileName());
                    try {
                        writeDecompiledClassToZip(file);
                        stats.recordDecompiledClass();
                    } catch (DecompilationException ex) {
                        stats.recordDecompiledErrors();
                        Path faultyDecompiledJavaClass = ex.getFaultyFile();
                        if (Files.exists(faultyDecompiledJavaClass)) {
                            Files.delete(faultyDecompiledJavaClass);
                        }
                        logger.debug("Exception during decompilation", ex);
                    }
                } else {
                    logger.debug("Since {} is not a class file. Just copy it to destionation zip", file);
                    Files.copy(file, destinationZipFileSystem.getPath(file.toString()));
                }

                return FileVisitResult.CONTINUE;
            }

            private void writeDecompiledClassToZip(Path classFile) throws DecompilationException {
                Path javaFilePath = Paths.get(classFile.toString().replace(".class", ".java"));
                Path javaFileInZip = destinationZipFileSystem.getPath(javaFilePath.toString());

                try {
                    /* dont't close this stream otherwise the zip file is not readable */
                    OutputStream javaFileInZipOutputStream = Files.newOutputStream(javaFileInZip);
                    try (InputStream compileClassInJarInputStream = Files.newInputStream(classFile)) {
                        decompileClass(compileClassInJarInputStream, javaFileInZipOutputStream);
                    }
                } catch (IOException | DecompilationException ex) {
                    throw new DecompilationException(ex, classFile);
                }
            }

            private boolean fileIsAClass(Path file) {
                return file.getFileName().toString().endsWith(".class");
            }
        });
        destinationZipFileSystem.close();

        return stats;
    }
    
    public abstract void decompileClass(InputStream compiledClass, OutputStream decompiledClass) throws DecompilationException;
}

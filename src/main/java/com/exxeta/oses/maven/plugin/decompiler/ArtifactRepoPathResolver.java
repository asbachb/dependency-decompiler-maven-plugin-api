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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.artifact.Artifact;

/**
 *
 * @author <a href="mailto:benjamin.asbach@exxeta.com">Benjamin Asbach, 2015</a>
 */
public class ArtifactRepoPathResolver {

    private ArtifactRepoPathResolver() {
    }

    public static Path getRepoArtifactSourcePath(Artifact artifact, String repoPath) {
        return getRepoArtifactPath(artifact, repoPath, "-sources.jar");
    }

    public static Path getRepoArtifactPath(Artifact artifact, String repoPath) {
        return getRepoArtifactPath(artifact, repoPath, ".jar");
    }

    private static Path getRepoArtifactPath(Artifact artifact, String repoPath, String suffix) {
        String relativeArtifactPath
                = artifact.getGroupId().replace('.', File.separatorChar)
                + File.separatorChar
                + artifact.getArtifactId()
                + File.separatorChar + artifact.getVersion();
        String artifactSourceName
                = artifact.getArtifactId()
                + "-"
                + artifact.getVersion()
                + suffix;
        return Paths.get(repoPath, relativeArtifactPath, artifactSourceName);
    }
}

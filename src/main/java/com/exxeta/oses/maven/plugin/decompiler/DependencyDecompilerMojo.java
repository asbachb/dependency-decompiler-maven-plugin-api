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

import com.exxeta.oses.maven.plugin.decompiler.resolution.ArtifactSourcesResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;

public class DependencyDecompilerMojo extends AbstractMojo {

    @Inject
    private MavenSession maven;

    @Inject
    private MavenProject mavenProject;

    @Inject
    private JarDecompiler jarDecompiler;

    @Inject
    private TransitiveDependencyResolver transitiveDependencyResolver;

    @Inject
    private ArtifactSourcesResolver artifactSourcesResolver;

    @Inject
    private MavenSession mavenSession;

    @Parameter(property = "localRepository", required = true, readonly = true)
    private ArtifactRepository local;

    @Parameter(property = "project.remoteArtifactRepositories", required = true, readonly = true)
    private List<ArtifactRepository> remoteRepos;

    @Parameter(property = "artifactIdRegex", defaultValue = ".*")
    private String artifactIdRegex;

    @Parameter(property = "groupIdRegex", defaultValue = ".*")
    private String groupIdRegex;

    @Parameter(property = "versionRegex", defaultValue = ".*")
    private String versionRegex;

    @Override
    public void execute() throws MojoExecutionException {
        String repoBase = maven.getLocalRepository().getBasedir();

        try {
            Set<Artifact> transitiveDependencies = transitiveDependencyResolver.getAllTransiticeDependencies(
                    mavenProject,
                    new CombinedArtifactFilter(
                            Arrays.asList(new ArtifactFilter[]{
                                (ArtifactFilter) new RegexArtifactFilter(artifactIdRegex, groupIdRegex, versionRegex),
                                (ArtifactFilter) new AvailableSourceArtifactFilter(mavenSession.getLocalRepository().getBasedir())
                            })
                    )
            );
            if (transitiveDependencies.isEmpty()) {
                getLog().info("No dependencies with unresolved sources found.");
                return;
            }
            ArtifactSourcesResolver.Conclusion conclusion = artifactSourcesResolver.resolveArtifactSources(
                    transitiveDependencies,
                    local,
                    remoteRepos
            );

            for (Artifact dependency : conclusion.getUnresolved()) {
                Path dependencyRepoJar = ArtifactRepoPathResolver.getRepoArtifactPath(dependency, repoBase);
                Path dependencyRepoSourceJar = ArtifactRepoPathResolver.getRepoArtifactSourcePath(dependency, repoBase);
                DecompilationStatistics decompilationStats = jarDecompiler.decompileJar(
                        dependencyRepoJar,
                        dependencyRepoSourceJar
                );
                getLog().info(String.format(
                        "Finished decompilation of %s [%d classes successful, %d classes failed]",
                        dependencyRepoJar.getFileName(),
                        decompilationStats.getDecompiledClasses(),
                        decompilationStats.getDecompilationErrors()
                ));
            }
        } catch (DependencyGraphBuilderException | ArtifactResolutionException | IOException ex) {
            throw new DependencyDecompilerMojoException("Could not get transitive dependencies", ex);
        }
    }
}

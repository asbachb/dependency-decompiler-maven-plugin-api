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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyGraphBuilder;

/**
 *
 * @author <a href="mailto:benjamin.asbach@exxeta.com">Benjamin Asbach, 2015</a>
 */
public class TransitiveDependencyResolver {

    @Inject
    private DefaultDependencyGraphBuilder defaultDependencyGraphBuilder;

    public Set<Artifact> getAllTransiticeDependencies(MavenProject mavenProject, ArtifactFilter artifactFilter) throws DependencyGraphBuilderException {
        Set<Artifact> transitiveDependencies = new HashSet<>();

        DependencyNode dependencyGraph = defaultDependencyGraphBuilder.buildDependencyGraph(
                mavenProject,
                null
        );
        collectAllTransitiveDependencies(transitiveDependencies, dependencyGraph);
        filterCollectedDependencies(transitiveDependencies, artifactFilter);

        return transitiveDependencies;
    }

    private void collectAllTransitiveDependencies(Set<Artifact> transitiveDependencies, DependencyNode node) {
        for (DependencyNode child : node.getChildren()) {
            transitiveDependencies.add(child.getArtifact());
            collectAllTransitiveDependencies(transitiveDependencies, child);
        }
    }
    
    private void filterCollectedDependencies(Set<Artifact> transitiveDependencies, ArtifactFilter artifactFilter) {
        Iterator<Artifact> iterator = transitiveDependencies.iterator();
        while (iterator.hasNext()) {
            Artifact transitiveDependency = iterator.next();
            if (!artifactFilter.include(transitiveDependency)) {
                iterator.remove();
            }
        }
    }
}

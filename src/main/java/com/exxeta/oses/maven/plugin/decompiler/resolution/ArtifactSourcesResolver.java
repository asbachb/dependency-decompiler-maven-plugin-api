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
package com.exxeta.oses.maven.plugin.decompiler.resolution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;

/**
 *
 * @author <a href="mailto:benjamin.asbach@exxeta.com">Benjamin Asbach, 2015</a>
 */
public class ArtifactSourcesResolver {

    @Inject
    private ArtifactFactory factory;

    @Inject
    private ArtifactResolver artifactsResolver;

    public Conclusion resolveArtifactSources(
            Set<Artifact> artifacts,
            ArtifactRepository local,
            List<ArtifactRepository> remoteRepos) throws ArtifactResolutionException {
        Set<Artifact> resolvedArtifacts = new HashSet<>(artifacts.size());
        Set<Artifact> unresolvedArtifacts = new HashSet<>(artifacts.size());

        for (Artifact artifact : artifacts) {
            Artifact sourceArtifact = translateIntoSourceArtifact(artifact);
            try {
                artifactsResolver.resolve(sourceArtifact, remoteRepos, local);
                resolvedArtifacts.add(sourceArtifact);
            } catch (ArtifactNotFoundException ex) {
                unresolvedArtifacts.add(sourceArtifact);
            }
        }

        return new Conclusion(resolvedArtifacts, unresolvedArtifacts);
    }

    private Artifact translateIntoSourceArtifact(Artifact artifact) {
        return factory.createArtifactWithClassifier(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(),
                "jar",
                "sources"
        );
    }

    public class Conclusion {

        private final Set<Artifact> resolved;
        private final Set<Artifact> unresolved;

        public Conclusion(Set<Artifact> resolved, Set<Artifact> unresolved) {
            this.resolved = resolved;
            this.unresolved = unresolved;
        }

        public Set<Artifact> getResolved() {
            return resolved;
        }

        public Set<Artifact> getUnresolved() {
            return unresolved;
        }
    }
}

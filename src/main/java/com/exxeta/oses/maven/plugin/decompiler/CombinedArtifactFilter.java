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

import java.util.Collection;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

/**
 *
 * @author <a href="mailto:benjamin.asbach@exxeta.com">Benjamin Asbach, 2015</a>
 */
public class CombinedArtifactFilter implements ArtifactFilter {

    private final Collection<ArtifactFilter> artifactFilters;

    public CombinedArtifactFilter(Collection<ArtifactFilter> artifactFilters) {
        this.artifactFilters = artifactFilters;
    }

    @Override
    public boolean include(Artifact artifact) {
        for (ArtifactFilter artifactFilter : artifactFilters) {
            if (!artifactFilter.include(artifact)) {
                return false;
            }
        }

        return true;
    }
}
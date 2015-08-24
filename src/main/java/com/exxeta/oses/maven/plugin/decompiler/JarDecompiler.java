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
import java.nio.file.Path;

/**
 *
 * @author <a href="mailto:benjamin.asbach@exxeta.com">Benjamin Asbach, 2015</a>
 */
public interface JarDecompiler {

    DecompilationStatistics decompileJar(Path jarToDecompile, Path destination) throws IOException;
}

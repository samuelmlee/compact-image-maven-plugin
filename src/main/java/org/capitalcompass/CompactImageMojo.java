package org.capitalcompass;

import java.io.File;
import java.io.IOException;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "compact-image", defaultPhase = LifecyclePhase.PACKAGE)
public class CompactImageMojo extends AbstractMojo {

	@Parameter(property = "project.name", required = true)
	private String projectName;

	@Parameter(property = "version", required = true)
	private String version;

	public void execute() throws MojoExecutionException {
		try {

			File extractedDir = new File("target/extracted");
			if (!extractedDir.exists()) {
				extractedDir.mkdirs();
			}

			// Extract the JAR using layertools
			getLog().info("Extracting JAR using layertools...");
			String jarPath = "target/" + projectName + "-" + version + ".jar";
			ProcessBuilder extractJar = new ProcessBuilder("java", "-Djarmode=layertools", "-jar", jarPath, "extract",
					"--destination", "target/extracted");
			extractJar.inheritIO();
			Process extractProcess = extractJar.start();
			extractProcess.waitFor();

			// Build Docker image
			getLog().info("Building Docker image...");
			ProcessBuilder dockerBuild = new ProcessBuilder("docker", "build", "-t",
					"samuelmlee/" + projectName + ":v" + version, ".");
			dockerBuild.inheritIO();
			Process dockerProcess = dockerBuild.start();
			dockerProcess.waitFor();

		} catch (IOException | InterruptedException e) {
			throw new MojoExecutionException("Error executing build-image goal", e);
        }
    }
}

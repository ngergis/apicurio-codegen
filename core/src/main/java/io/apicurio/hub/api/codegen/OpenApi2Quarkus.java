/*
 * Copyright 2018 JBoss Inc
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

package io.apicurio.hub.api.codegen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import io.apicurio.hub.api.codegen.beans.CodegenInfo;


/**
 * Class used to generate a Quarkus JAX-RS project from an OpenAPI document.
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApi2Quarkus extends OpenApi2JaxRs {

    /**
     * Constructor.
     */
    public OpenApi2Quarkus() {
        super();
    }

    /**
     * @see io.apicurio.hub.api.codegen.OpenApi2JaxRs#generateAll(io.apicurio.hub.api.codegen.beans.CodegenInfo, java.lang.StringBuilder, java.util.zip.ZipOutputStream)
     */
    @Override
    protected void generateAll(CodegenInfo info, StringBuilder log, ZipOutputStream zipOutput)
            throws IOException {
        super.generateAll(info, log, zipOutput);

        if (!this.isUpdateOnly() && !this.settings.codeOnly) {
            log.append("Generating Dockerfiles\r\n");
            Stream.of("jvm", "legacy-jar", "native", "native-micro")
                .map("src/main/docker/Dockerfile."::concat)
                .forEach(path -> writeEntry(zipOutput, path));

            log.append("Generating application.properties\r\n");
            zipOutput.putNextEntry(new ZipEntry("src/main/resources/application.properties"));
            zipOutput.write(generateApplicationProperties().getBytes());
            zipOutput.closeEntry();

            log.append("Generating project files\r\n");
            writeEntry(zipOutput, ".mvn/wrapper/.gitignore");
            writeEntry(zipOutput, ".mvn/wrapper/maven-wrapper.properties");
            writeEntry(zipOutput, ".mvn/wrapper/MavenWrapperDownloader.java");
            writeEntry(zipOutput, ".dockerignore");
            writeEntry(zipOutput, ".gitignore");
            writeEntry(zipOutput, "mvnw");
            writeEntry(zipOutput, "mvnw.cmd");
            writeEntry(zipOutput, "README.md");
        }
    }

    /**
     * @see io.apicurio.hub.api.codegen.OpenApi2JaxRs#generateJaxRsApplication()
     */
    @Override
    protected String generateJaxRsApplication() {
        // Don't need one of these for Quarkus.
        return null;
    }

    private void writeEntry(ZipOutputStream zipOutput, String sourcePath) {
        try {
            zipOutput.putNextEntry(new ZipEntry(sourcePath));
            zipOutput.write(IOUtils.toString(getResource(sourcePath), StandardCharsets.UTF_8).getBytes());
            zipOutput.closeEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Generates the openshift-template.yml file.
     */
    private String generateApplicationProperties() throws IOException {
        String template = IOUtils.toString(getResource("src/main/resources/application.properties"), Charset.forName("UTF-8"));
        template += "\nmp.openapi.scan.disable=true\n";
        return template;
    }

}

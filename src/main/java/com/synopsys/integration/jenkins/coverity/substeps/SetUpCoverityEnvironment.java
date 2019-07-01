/**
 * synopsys-coverity
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.jenkins.coverity.substeps;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.coverity.executable.CoverityToolEnvironmentVariable;
import com.synopsys.integration.jenkins.coverity.GlobalValueHelper;
import com.synopsys.integration.jenkins.coverity.JenkinsCoverityEnvironmentVariable;
import com.synopsys.integration.jenkins.coverity.JenkinsCoverityLogger;
import com.synopsys.integration.jenkins.coverity.exception.CoverityJenkinsException;
import com.synopsys.integration.jenkins.coverity.extensions.global.CoverityConnectInstance;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class SetUpCoverityEnvironment {
    private final JenkinsCoverityLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final String coverityInstanceUrl;
    private final String projectName;
    private final String streamName;
    private final String viewName;
    private final List<String> changeSet;
    private final String pathToCoverityToolHome;

    public SetUpCoverityEnvironment(final JenkinsCoverityLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final String pathToCoverityToolHome, final String coverityInstanceUrl, final String projectName,
        final String streamName, final String viewName, final List<String> changeSet) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.coverityInstanceUrl = coverityInstanceUrl;
        this.projectName = projectName;
        this.streamName = streamName;
        this.viewName = viewName;
        this.changeSet = changeSet;
        this.pathToCoverityToolHome = pathToCoverityToolHome;
    }

    public void setUpCoverityEnvironment() throws CoverityJenkinsException {
        final CoverityConnectInstance coverityInstance = GlobalValueHelper.getCoverityInstanceWithUrl(logger, coverityInstanceUrl).orElseThrow(GlobalValueHelper.COULD_NOT_FIND_INSTANCE(coverityInstanceUrl));

        if (StringUtils.isBlank(pathToCoverityToolHome)) {
            throw new CoverityJenkinsException("Could not get path to Coverity tool home or the path provided is invalid.");
        }

        intEnvironmentVariables.put("PATH+COVERITYTOOLBIN", pathToCoverityToolHome);
        intEnvironmentVariables.put(CoverityToolEnvironmentVariable.USER.toString(), coverityInstance.getCoverityUsername().orElse(StringUtils.EMPTY));
        intEnvironmentVariables.put(CoverityToolEnvironmentVariable.PASSPHRASE.toString(), coverityInstance.getCoverityPassword().orElse(StringUtils.EMPTY));
        intEnvironmentVariables.put(JenkinsCoverityEnvironmentVariable.COVERITY_URL.toString(), coverityInstance.getUrl());
        intEnvironmentVariables.put(JenkinsCoverityEnvironmentVariable.COVERITY_PROJECT.toString(), projectName);
        intEnvironmentVariables.put(JenkinsCoverityEnvironmentVariable.COVERITY_STREAM.toString(), streamName);
        intEnvironmentVariables.put(JenkinsCoverityEnvironmentVariable.COVERITY_VIEW.toString(), viewName);
        intEnvironmentVariables.put(JenkinsCoverityEnvironmentVariable.CHANGE_SET.toString(), String.join(" ", changeSet));
        intEnvironmentVariables.put(JenkinsCoverityEnvironmentVariable.CHANGE_SET_SIZE.toString(), String.valueOf(changeSet.size()));
        intEnvironmentVariables.put(JenkinsCoverityEnvironmentVariable.COVERITY_INTERMEDIATE_DIRECTORY.toString(), computeIntermediateDirectory());

        logger.alwaysLog("Synopsys Coverity environment:");
        logger.alwaysLog("-- Synopsys Coverity static analysis tool home: " + pathToCoverityToolHome);
        logger.alwaysLog("-- Synopsys Coverity username: " + coverityInstance.getCoverityUsername().orElse(StringUtils.EMPTY));
        Arrays.stream(JenkinsCoverityEnvironmentVariable.values())
            .map(JenkinsCoverityEnvironmentVariable::toString)
            .map(environmentVariable -> String.format("-- $%s=%s", environmentVariable, intEnvironmentVariables.getValue(environmentVariable)))
            .forEach(logger::alwaysLog);
    }

    private String computeIntermediateDirectory() {
        final String workspace = intEnvironmentVariables.getValue("WORKSPACE");
        final Path workspacePath = Paths.get(workspace);
        final Path intermediateDirectoryPath = workspacePath.resolve("idir");
        return intermediateDirectoryPath.toString();
    }

}

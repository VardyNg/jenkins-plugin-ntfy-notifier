package io.jenkins.plugins.sample;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NTFYNotifierBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String name = "Bobby";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new NTFYNotifierBuilder(name));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
                new NTFYNotifierBuilder(name), project.getBuildersList().get(0));
    }

    @Test
    public void testConfigRoundtripFrench() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        NTFYNotifierBuilder builder = new NTFYNotifierBuilder(name);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        NTFYNotifierBuilder lhs = new NTFYNotifierBuilder(name);
        lhs.setUseFrench(true);
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        NTFYNotifierBuilder builder = new NTFYNotifierBuilder(name);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Hello, " + name, build);
    }

    @Test
    public void testBuildFrench() throws Exception {

        FreeStyleProject project = jenkins.createFreeStyleProject();
        NTFYNotifierBuilder builder = new NTFYNotifierBuilder(name);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Bonjour, " + name, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {greet '" + name + "'}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Hello, " + name + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
}

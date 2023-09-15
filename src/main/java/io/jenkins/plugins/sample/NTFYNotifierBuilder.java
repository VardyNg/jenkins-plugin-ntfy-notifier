package io.jenkins.plugins.sample;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class NTFYNotifierBuilder extends Builder implements SimpleBuildStep {

    private final String serverURL;
    private final String topic;
    private final String message;

    @DataBoundConstructor
    public NTFYNotifierBuilder(String serverURL, String topic, String message) {
        this.serverURL = serverURL;
        this.topic = topic;
        this.message = message;
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        if (useFrench) {
            listener.getLogger().println("Bonjour, " + serverURL + "!");
        } else {
            listener.getLogger().println("Sending message to https://" + serverURL + "/" + topic);
        }
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.NTFYNotifyBuilder_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.NTFYNotifyBuilder_DescriptorImpl_warnings_tooShort());
            if (!useFrench && value.matches(".*[éáàç].*")) {
                return FormValidation.warning(Messages.NTFYNotifyBuilder_DescriptorImpl_warnings_reallyFrench());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.NTFYNotifyBuilder_DescriptorImpl_DisplayName();
        }
    }
}

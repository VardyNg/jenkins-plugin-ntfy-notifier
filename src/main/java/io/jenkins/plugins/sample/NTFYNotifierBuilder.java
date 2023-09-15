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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NTFYNotifierBuilder extends Builder implements SimpleBuildStep {

    private final String serverURL;
    private final String topic;
    private final String title;
    private final String message;
    private final String priority;
    private final String tags;
    private final Boolean useMarkdownFormatting;
    @DataBoundConstructor
    public NTFYNotifierBuilder(String serverURL, String topic, String message, String title, String priority, String tags, Boolean useMarkdownFormatting) {
        this.serverURL = serverURL;
        this.topic = topic;
        this.message = message;
        this.title = title;
        this.priority = priority;
        this.tags = tags;
        this.useMarkdownFormatting = useMarkdownFormatting;
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

        final String topicURL = "https://" + serverURL + "/" + topic;
        
        listener.getLogger().println("Sending message to " + topicURL);
        listener.getLogger().println("Message: " + message);
        listener.getLogger().println("Title: " + title);
        listener.getLogger().println("Priority: " + priority);
        listener.getLogger().println("Tags " + tags);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(topicURL).openConnection();

            // Set the request method (POST is the default but being explicit here).
            conn.setRequestMethod("POST");

            // set headers
            if (!title.isEmpty()) conn.setRequestProperty("Title", title);
            if (!priority.isEmpty()) conn.setRequestProperty("Priority", priority);
            if (!tags.isEmpty()) conn.setRequestProperty("Tags", tags.trim());
            conn.setRequestProperty("Markdown", useMarkdownFormatting ? "yes" : "no");
            // To send a POST request, we should set this to true
            conn.setDoOutput(true);

            // Write the body of the request
            conn.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Success
                listener.getLogger().println("Request was successful!");
            } else {
                // Handle any other responses here
                listener.getLogger().println("Failed with HTTP response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.getLogger().println("Failed to send message to https://" + serverURL + "/" + topic);
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

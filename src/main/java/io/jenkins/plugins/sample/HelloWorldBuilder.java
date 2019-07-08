package io.jenkins.plugins.sample;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.EnvVars;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;


public class HelloWorldBuilder extends Builder implements SimpleBuildStep {

    private final String branchid;
    private final String apikey;
    private final String filename;
    private final String environmentrelease;


    @DataBoundConstructor
    public HelloWorldBuilder(String branchid, String apikey, String filename, String environmentrelease) {
        this.branchid = branchid;
        this.apikey = apikey;
        this.filename = filename;
        this.environmentrelease = environmentrelease;
    }

    public String getBranchid() {
        return this.branchid;
    }


    public String getApikey() {
        return this.apikey;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getEnvironmentrelease() {
        return this.environmentrelease;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {   
        listener.getLogger().println("Filename  : " + filename );
        listener.getLogger().println("Branchname: " + branchid );
        listener.getLogger().println("API Key   :" + apikey );
        EnvVars envVars = new EnvVars();
        envVars = run.getEnvironment(listener);
        String myVersion = envVars.get(environmentrelease);
        // https://wiki.jenkins.io/display/JENKINS/Building+a+software+project
        listener.getLogger().println("Version   : " + myVersion );
        File myMobileApp = new File(workspace + "\\" + filename);
        if (myMobileApp.exists()) {
            listener.getLogger().println("Mobile file found!");
        } else {
            listener.getLogger().println("Mobile file not found!");
        }
    }
     

    @Symbol("myappciPlugin")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckBranchid(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingBranch());
            if (value.matches("^[a-zA-Z0-9.]+$") == false) 
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingBranchIdInvalid());
            return FormValidation.ok();
        }

        public FormValidation doCheckApiKey(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingApiKEy());
            if (value.length() <= 6) 
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_warnings_ApiKeytooShort());
            return FormValidation.ok();
        }
 
        public FormValidation doCheckEnvironmentrelease(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingEnviomnmentRelease());
 
            return FormValidation.ok();
        }


        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

}

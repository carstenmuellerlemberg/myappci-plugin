package io.jenkins.plugins.sample;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.AbortException;
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
import jenkins.tasks.SimpleBuildStep;


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
    
    @CheckForNull
    public String getBranchid() {
        return this.branchid;
    }

    @CheckForNull
    public String getApikey() {
        return this.apikey;
    }
    @CheckForNull
    public String getFilename() {
        return this.filename;
    }

    public String getEnvironmentrelease() {
        return this.environmentrelease;
    }

    private void uploadfile(String uploadFile, String branchname, String apikey, String version, TaskListener listener)
            throws ClientProtocolException, IOException {
        File file = new File(uploadFile);
        HttpPost post = new HttpPost("https://myappci.com/api/ci");
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        StringBody stringBranch = new StringBody(branchname, ContentType.MULTIPART_FORM_DATA);
        StringBody stringApiKey = new StringBody(apikey, ContentType.MULTIPART_FORM_DATA);
        StringBody stringVersion = new StringBody(version, ContentType.MULTIPART_FORM_DATA);
        // 
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("app", fileBody);
        builder.addPart("branch", stringBranch);
        builder.addPart("apikey", stringApiKey);
        builder.addPart("version", stringVersion);
        HttpEntity entity = builder.build();
        //
        post.setEntity(entity);
        CloseableHttpClient  client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(post);	
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity, "UTF-8");
            listener.getLogger().println("Response  : " + responseString );
            throw new AbortException("App publishing failed:  " + responseString);
        }
        listener.getLogger().println("Status  : " + statusCode );
	
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
        if (myVersion != null) {
            String myUploadfile = workspace + "\\" + filename;
            File myMobileApp = new File(myUploadfile);
            if (myMobileApp.exists()) {
                listener.getLogger().println("Mobile file found!");
                uploadfile(myUploadfile, branchid, apikey, myVersion, listener);
            } else {
                listener.getLogger().println("Mobile file not found!");
                throw new AbortException("Mobile file not found at " + myUploadfile);
            }
        } else {
            listener.getLogger().println("Environment variable for release version not defined!");
            throw new AbortException("Environment variable for release version not defined!");
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

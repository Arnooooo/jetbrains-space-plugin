package org.jetbrains.space.jenkins.config;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nullable;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isBlank;

public class SpaceConnection extends AbstractDescribableImpl<SpaceConnection> {

    private final String id;
    private final String name;
    private final String baseUrl;
    private final String apiCredentialId;
    private final String sshCredentialId;

    @DataBoundConstructor
    public SpaceConnection(@Nullable String id, String name, String baseUrl, String apiCredentialId, String sshCredentialId) {
        this.id = isBlank(id) ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.apiCredentialId = apiCredentialId;
        this.sshCredentialId = sshCredentialId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiCredentialId() {
        return apiCredentialId;
    }

    public String getSshCredentialId() {
        return sshCredentialId;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SpaceConnection> {

        @POST
        public ListBoxModel doFillApiCredentialIdItems(@QueryParameter String baseUrl) {
            Jenkins.get().checkPermission(Item.CONFIGURE);
            return new StandardListBoxModel()
                    .includeMatchingAs(
                            ACL.SYSTEM2,
                            Jenkins.get(),
                            SpaceApiCredentials.class,
                            URIRequirementBuilder.fromUri(baseUrl).build(),
                            CredentialsMatchers.always()
                    );
        }

        @POST
        public ListBoxModel doFillSshCredentialIdItems(@QueryParameter String baseUrl) {
            Jenkins.get().checkPermission(Item.CONFIGURE);
            return new StandardListBoxModel()
                    .includeMatchingAs(
                            ACL.SYSTEM2,
                            Jenkins.get(),
                            BasicSSHUserPrivateKey.class,
                            URIRequirementBuilder.fromUri(baseUrl).build(),
                            CredentialsMatchers.always()
                    );
        }

        @POST
        public FormValidation doTestApiConnection(
                @QueryParameter String baseUrl,
                @QueryParameter String apiCredentialId
        ) {
            Jenkins.get().checkPermission(Item.CONFIGURE);
            try {
                UtilsKt.testSpaceApiConnection(baseUrl, apiCredentialId);
            } catch (Throwable ex) {
                return FormValidation.error(ex, "Couldn't connect to JetBrains Space API");
            }

            return FormValidation.ok();
        }
    }
}
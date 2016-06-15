package org.keycloak.forge.addon;


import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.WebResourcesFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * Created by ggastald on 15/06/16.
 */
@FacetConstraint(WebResourcesFacet.class)
public class InstallClientJsonCommand extends AbstractProjectCommand {

    private static final String PROVIDER_ID = "keycloak-oidc-keycloak-json";

    private static final String KEYCLOAK_JSON_PATH = "WEB-INF/keycloak.json";

    @Inject
    @WithAttributes(label = "Server URL", description = "The keycloak server URL (eg. http://localhost:8080/auth)", required = true)
    private UIInput<String> serverUrl;

    @Inject
    @WithAttributes(label = "Realm", description = "The realm to be used (eg. master)", required = true)
    private UIInput<String> realm;

    @Inject
    @WithAttributes(label = "Client ID", description = "The client ID configured in the keycloak server (eg. security-admin-console). Make sure that Direct Access Grants Enabled is enabled for this client", required = true)
    private UIInput<String> clientId;

    @Inject
    @WithAttributes(label = "User", description = "Username to be authenticated", required = true)
    private UIInput<String> user;

    @Inject
    @WithAttributes(label = "Password", description = "Password for the authenticated user", required = true)
    private UIInput<String> password;

    @Inject
    private ProjectFactory projectFactory;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(serverUrl).add(realm).add(clientId).add(user).add(password);
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(InstallClientJsonCommand.class).category(Categories.create("Keycloak")).name("Keycloak: Install Client Json");
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Keycloak kc = KeycloakBuilder.builder()
                .serverUrl(serverUrl.getValue())
                .realm(realm.getValue())
                .clientId(clientId.getValue())
                .username(user.getValue())
                .password(password.getValue())
                .build();
        try {
            ClientsResource clientsResource = kc.realm(realm.getValue()).clients();
            List<ClientRepresentation> clientRepresentationList = clientsResource.findByClientId(clientId.getValue());
            ClientRepresentation clientRepresentation = clientRepresentationList.get(0);
            ClientResource clientResource = clientsResource.get(clientRepresentation.getId());
            String jsonContents = clientResource.getInstallationProvider(PROVIDER_ID);
            WebResourcesFacet facet = getSelectedProject(context).getFacet(WebResourcesFacet.class);
            facet.getWebResource(KEYCLOAK_JSON_PATH).setContents(jsonContents);
        } finally {
            kc.close();
        }
        return Results.success("JSON successfully created!");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }
}

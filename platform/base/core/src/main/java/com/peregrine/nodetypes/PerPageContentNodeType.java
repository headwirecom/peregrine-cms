package com.peregrine.nodetypes;

import com.peregrine.nodetypes.builder.Class3;
import com.peregrine.nodetypes.builder.NodeTypeDefinitionWrapper;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Extending the existing PerPageContent nodetype
 *
 * <p> </p></p><'per'='http://www.peregrine-cms.com/jcr/cms/1.0'>
 *
 * <p> [per:PageContent] > nt:unstructured, sling:Resource, sling:VanityPath, per:Replication
 * <p>  orderable
 * <p>  - protocol(string) = 'https://' mandatory autocreated
 * <p>  - hostname(string) = 'www.example.com' mandatory autocreated
 * <p>  - canonicalLink(string) = {@link #getCanonicalLink(String)}' mandatory autocreated
 * <p>  - excludeFromNavigation(boolean) = 'false' mandatory autocreated
 */
@Component(immediate = true)
public class PerPageContentNodeType extends AbstractNodeType {

  @Reference
  SlingRepository slingRepository;

  @Reference
  ResourceResolverFactory resourceResolverFactory;

  @Override
  public SlingRepository getSlingRepository() {
    return slingRepository;
  }

  @Override
  public ResourceResolverFactory getResourceResolverFactory() {
    return resourceResolverFactory;
  }

  @Override
  protected void registerNodeType() throws RepositoryException {

  }

  @Activate
  public void activate() throws Exception {
    Session session = null;
    try {
      session = slingRepository.loginAdministrative(null);
      NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();

      NodeTypeDefinitionWrapper nodetype = new NodeTypeDefinitionWrapper.Builder<>()
          .withName("per:Component")
          .build();






    } catch (LoginException e) {
      log.error("Could not get session.", e);
    } catch (RepositoryException e) {
      log.error("Could not get session.", e);
    }
  }

  private static String getCanonicalLink(final String resourcePath) {
    return "https://www.example.com";
  }
}
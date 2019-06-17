package com.peregrine.nodetypes;

import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE;
import static com.peregrine.commons.util.PerConstants.SLING_VANITY_PATH;

import com.peregrine.nodetypes.builder.GenericBuilder;
import com.peregrine.nodetypes.builder.NodeType;
import com.peregrine.nodetypes.builder.NodeTypeBuilder;
import com.peregrine.nodetypes.builder.Parent;
import java.util.Map;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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

  private static final String[] perPageContentSupertypes =
      new String[]{NT_UNSTRUCTURED, SLING_RESOURCE, SLING_VANITY_PATH, PER_REPLICATION};

//  @Reference
//  private ResourceResolverFactory resourceResolverFactory;

  @Reference
  private SlingRepository slingRepository;

  private Session session;
  private NodeTypeManager nodeTypeManager;

  @Activate
  protected void activate(Map<String, Object> properties) {
    try {
      session = slingRepository.loginAdministrative(null);
      nodeTypeManager = session.getWorkspace().getNodeTypeManager();


//      Person value = GenericBuilder.of(Person::new)
//          .with(Person::setName, "Otto")
//          .with(Person::setAge, 5)
//          .build();

      NodeTypeBuilder nodetype = NodeTypeBuilder.newBuilder()
          .witName("")
          .withAbstract("")
          .withMixin("")
          .build();

    } catch (LoginException e) {
      log.error("Could not get session.", e);
    } catch (RepositoryException e) {
      log.error("Could not get session.", e);
    }
  }

  @Deactivate
  protected void deactivate(ComponentContext ctx) {
    if (session != null && session.isLive()) {
      session.logout();
      session = null;
    }
  }









  @Override
  public Session getSession() {
    return session;
  }

  @Override
  public NodeTypeManager getNodeTypeManager() {
    return nodeTypeManager;
  }

  @Override
  protected void registerNodeType() throws RepositoryException {

    NodeTypeTemplate type = new AbstractNodeType().Builder()
        .createNodeType()
        .setPropertyDefinition()
        .setNodeDefinition()
        .build();


  }

  private static String getCanonicalLink(final String resourcePath) {
    return "https://www.example.com";
  }
}
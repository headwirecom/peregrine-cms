package com.peregrine.nodetypes;

import static com.peregrine.commons.util.PerConstants.PAGE_PROPERTIES;

import com.peregrine.nodetypes.builder.NodeTypeTemplateFactory;
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
 * Creating mixin NodeType called per:PageProperties to add additional properties to
 * 'per:PageContent' nodetype
 *
 * <p> [per:PageProperties]
 * <p>  mixin
 * <p>  - protocol(string) = 'https://' mandatory autocreate
 * <p>  - hostname(string) = 'www.example.com' mandatory autocreate
 * <p>  - canonicalLink(string) = {@link #getCanonicalLink(String)}' mandatory autocreate
 * <p>  - excludeFromNavigation(boolean) = 'false' mandatory autocreate
 */
@Component
public class PagePropertiesNodeType extends AbstractNodeType {

  @Reference
  SlingRepository slingRepository;

  private Session session;

  @Activate
  protected void activate(ComponentContext context) throws Exception {
    try {
      session = slingRepository.loginAdministrative(null);
      registerNodeType(session);

    } catch (LoginException e) {
      log.error("Failed to login as default 'admin'");
    } catch (RepositoryException e) {
      log.error("Could not return a session to the given workspace.", e);
    }
  }

  @Deactivate
  protected void deactivate(ComponentContext componentContext) {
    if (session != null) {
      session.logout();
      session = null;
    }
  }

  @Override
  protected void registerNodeType(Session session) throws RepositoryException {
    try {
      NodeTypeManager manager = session.getWorkspace().getNodeTypeManager();

      NodeTypeTemplate template = (NodeTypeTemplate) NodeTypeTemplateFactory
          .newNodeTypeDefinitionBuilder(session)
          .setName(PAGE_PROPERTIES)
          .setAbstract(false)
          .setMixin(true)
          .setQueryable(false)
          .setOrderableChildNodes(true)
          .build();

      log.info("Registered NodeType: " + template.getName());
      manager.registerNodeType(template, true);

    } catch (RepositoryException e) {
      log.error(e.getMessage());
    }
    session.save();
  }

  private static String getCanonicalLink(final String resourcePath) {
    return "https://www.example.com";
  }
}
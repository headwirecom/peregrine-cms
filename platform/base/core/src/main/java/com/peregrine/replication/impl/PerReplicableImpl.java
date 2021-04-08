package com.peregrine.replication.impl;

import com.peregrine.adaption.impl.PerBaseImpl;
import com.peregrine.replication.PerReplicable;
import com.peregrine.commons.util.PerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import java.util.*;

import static com.peregrine.commons.util.PerConstants.*;

public class PerReplicableImpl extends PerBaseImpl implements PerReplicable {

    private static final Logger LOG = LoggerFactory.getLogger(PerReplicableImpl.class);

    private ValueMap vm;
    private ModifiableValueMap modifiableValueMap;

    public PerReplicableImpl(Resource resource) {
        super(resource);
        vm = Optional.ofNullable(getProperties())
                .orElseGet(resource::getValueMap);
        modifiableValueMap = Optional.ofNullable(getModifiableProperties())
                .orElseGet(() -> resource.adaptTo(ModifiableValueMap.class));
    }

    private <T> T getProperty(final String name, final Class<T> type) {
        return vm.get(name, type);
    }

    private String getStringProperty(final String name) {
        return getProperty(name, String.class);
    }

    private Calendar getCalendarProperty(String jcrLastModified) {
        return getProperty(jcrLastModified, Calendar.class);
    }

    /**
     * Is Replicated makes an inference about the replication status based on the Per:ReplicationRef property
     * If it exists but empty; the inference is the resource was deactivated
     * If the value of Per:ReplicationRef starts with AGENT, the resource was "activated"
     * If the value of Per:ReplicationRef starts with IMPORTER, the resource was imported as a result of replication
     * @return true if the resource has been replicated
     */
    @Override
    public boolean isReplicated() {
        return Optional.ofNullable(getStringProperty(PER_REPLICATION_REF))
                .map(StringUtils::isNotBlank)
                .orElse(false);
    }

    /**
     * @return true if the resource is has per:ReplicationStatus 'activated' and content was modified after the per:Replicated date
     */
    @Override
    public boolean isStale() {
        return Optional.ofNullable(getCalendarProperty(JCR_LAST_MODIFIED))
                .map(lm -> lm.after(getReplicated()))
                .orElse(false);
    }

    /**
     * @return the date for the latest replication status
     */
    @Override
    public Calendar getReplicated() {
        return getCalendarProperty(PER_REPLICATED);
    }

    /**
     * @return A string property reference about the replication
     */
    @Override
    public String getReplicationRef() {
        return getStringProperty(PER_REPLICATION_REF);
    }

    @Override
    public String getLastReplicationAction(){
        return getStringProperty(PER_REPLICATION_LAST_ACTION);
    }

    @Override
    public boolean ensureReplicableMixin() {
        final Resource resource = hasContent() ? getContentResource() : getResource();
        if (PerUtil.isPrimaryType(resource, NT_UNSTRUCTURED,
                ASSET_CONTENT_TYPE,
                "per:NpmPackageConfig",
                OBJECT_PRIMARY_TYPE,
                OBJECT_DEFINITION_PRIMARY_TYPE,
                PAGE_PRIMARY_TYPE,
                PAGE_CONTENT_TYPE)) {
            return true;
        }
        final Node node = resource.adaptTo(Node.class);
        try {
            if (!Arrays.stream(node.getMixinNodeTypes())
                    .map(NodeType::getName)
                    .anyMatch(PER_REPLICATION::equals)) {
                node.addMixin(PER_REPLICATION);
                node.getSession().save();
            }

            return true;
        } catch (final RepositoryException e) {
            return false;
        }
    }

    @Override
    public void setLastReplicationActionAsActivated() {
        setLastAction(ACTIVATED);
    }

    @Override
    public void setLastReplicationActionAsDeactivated(){
        setLastAction(DEACTIVATED);
    }

    private void setLastAction(String value){
        this.modifiableValueMap.put(PER_REPLICATION_LAST_ACTION, value);
        try {
            this.getResource().getResourceResolver().commit();
        } catch (PersistenceException e) {
            LOG.error("Could not set replication status to pending", e);
        }
    }
}

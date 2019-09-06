package com.peregrine.admin.migration;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.migration.MigrationResponse.MIGRATION_EXCEPTION_CODE;
import static com.peregrine.admin.migration.MigrationResponse.NO_MIGRATION_ACTION_FOUND_CODE;
import static com.peregrine.admin.migration.MigrationResponse.SUCCESS;
import static com.peregrine.admin.migration.MigrationResponse.UNEXPECTED_EXCEPTION_CODE;

/**
 * Migration Service that provides the List of registered Migration Action and executes it if found
 *
 * Created by Andreas Schaefer on 8/22/19.
 */
@Component(
    service = Migration.class,
    immediate = true
)
public class MigrationService
    implements Migration
{
    public static final String MIGRATION_EXCEPTION_MESSAGE = "Migration Exception was thrown by Migration Action: '%s'";
    public static final String UNEXPECTED_EXCEPTION_MESSAGE = "Unexpected Exception was thrown by Migration Action: '%s'";
    public static final String MIGRATION_ACTION_NOT_FOUND_MESSAGE = "Unexpected Exception was thrown by Migration Action: '%s'";

    private List<MigrationAction> migrationActionList = new ArrayList<>();

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    public void bindMigrationAction(MigrationAction migrationAction) {
        migrationActionList.add(migrationAction);
    }

    public void unbindMigrationAction(MigrationAction migrationAction) {
        migrationActionList.remove(migrationAction);
    }

    public List<MigrationDescriptor> getMigrationActions() {
        List<MigrationDescriptor> answer = new ArrayList<>();
        for(MigrationAction migrationAction: migrationActionList) {
            answer.add(migrationAction.getDescriptor());
        }
        return answer;
    }

    public MigrationResponse execute(String migrationActionName, ResourceResolver resourceResolver) {
        MigrationResponse answer = new MigrationResponse();
        MigrationAction target = null;
        for(MigrationAction migrationAction: migrationActionList) {
            if(migrationAction.getDescriptor().getName().equals(migrationActionName)) {
                target = migrationAction;
                break;
            }
        }
        if(target != null) {
            try {
                target.execute(resourceResolver);
                answer.setCode(SUCCESS)
                    .setMessage("Action Successfully Executed: '" + target.getDescriptor().getName() + "'")
                    .setMigrationDescriptor(target.getDescriptor());
            } catch(MigrationException e) {
                answer.setCode(MIGRATION_EXCEPTION_CODE)
                    .setException(e)
                    .setMessage(String.format(MIGRATION_EXCEPTION_MESSAGE, target.getDescriptor().getName()));
            } catch(Exception e) {
                answer.setCode(UNEXPECTED_EXCEPTION_CODE)
                    .setException(e)
                    .setMessage(String.format(UNEXPECTED_EXCEPTION_MESSAGE, target.getDescriptor().getName()));
            }
        } else {
            answer.setCode(NO_MIGRATION_ACTION_FOUND_CODE)
                .setMessage(String.format(MIGRATION_ACTION_NOT_FOUND_MESSAGE, migrationActionName));
        }
        return answer;
    }
}

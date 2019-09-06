package com.peregrine.admin.migration;

import com.peregrine.adaption.PerAsset;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.resource.AdminResourceHandlerService.handleAssetDimensions;
import static com.peregrine.commons.util.PerConstants.ASSETS_ROOT;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;

@Component(
    service = MigrationAction.class,
    immediate = true
)
public class AssetDimensionMigrationAction
    implements MigrationAction
{
    @Override
    public MigrationDescriptor getDescriptor() {
        return new MigrationDescriptor(
            "assetDimension",
            "Adds Dimensions to any Assets in Peregrine if not already there",
            "08/29/2019"
        );
    }

    @Override
    public void execute(ResourceResolver resourceResolver) throws MigrationException {
        // Find the Root of the Assets
        Resource root = resourceResolver.getResource(ASSETS_ROOT);
        if(root != null) {
            // Loop over all folders and if an Asset it hit then handle it
            List<String> failedAssets = new ArrayList<>();
            handleFolder(root, failedAssets);
            if(!failedAssets.isEmpty()) {
                throw new MigrationException("Failed to handle these assets: " + failedAssets);
            } else {
                try {
                    resourceResolver.commit();
                } catch (PersistenceException e) {
                    throw new MigrationException("Commit failure after the Asset Dimension Migration");
                }
            }
        } else {
            throw new MigrationException("Asset Root: '" + ASSETS_ROOT + "' not found");
        }
    }

    /** Loop over folders and either handle children as assets or as sub folders **/
    private void handleFolder(Resource folder, List<String> failedAssets) {
        for(Resource child: folder.getChildren()) {
            ValueMap properties = child.getValueMap();
            String resourceType = properties.get(JCR_PRIMARY_TYPE, String.class);
            if (ASSET_PRIMARY_TYPE.equals(resourceType)) {
                handleAsset(child, failedAssets);
            } else {
                // If this is not an Asset we assume a folder and delve into it
                handleFolder(child, failedAssets);
            }
        }
    }
    /** Handle a Resource that is an Asset **/
    private void handleAsset(Resource assetResource, List<String> failedAssets) {
        PerAsset asset = assetResource.adaptTo(PerAsset.class);
        if (asset != null) {
            try {
                handleAssetDimensions(asset);
            } catch (RepositoryException | IOException e) {
                failedAssets.add(asset.getName() + "(Message: " + e.getLocalizedMessage() + ")");
            }
        } else {
            failedAssets.add(assetResource.getName() + "(Failed to Adapt to PerAsset)");
        }
    }
}

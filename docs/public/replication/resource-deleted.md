# Replicate deleted Page

**Actor**: Peregrine CMS Author  
**Description**: Author deletes a Resource.

## Preconditions:
1. Resource already exists in the system.
1. Resource is already replicated.

## Postconditions:
1. All selected resources associated with the Resource are now updated in replication cache.
1. The replication status icon gets updated.

## Main Course:
1. Author deletes a Resource.
1. System presents the author with the dialog of choice to un-publish the Resource.
1. Author confirms the operation.
1. System follows the [Un-Publish](resource-unpublished.md) scenario.

## Alternate Course:
1. Author may choose not to un-publish the deleted page.
1. Author may choose to exclude some resources from replication cache cleansing.
1. Author may cancel the operation.
# Un-publish Resource

**Actor**: Peregrine CMS Author  
**Description**: Author un-publishes a Resource.

## Preconditions:
1. Resource already exists in the system.
1. Resource is already replicated.

## Postconditions:
1. All selected resources associated with the Resource are now updated in replication cache.
1. The replication status icon gets updated.

## Main Course:
1. Author un-publishes a Resource.
1. System presents the author with a list of associated resources that
   will get deleted, replicated or updated along. The list presents the status change:
   whether the item will be updated in replication target or deleted.
    * System presents the resources that are referenced from our Resource.
    * System presents the resources that reference our Resource and will be affected.
1. Author confirms the operation.
1. The system analyzes the list of resources from the point of view
   of missing references analogously to how it is handled within
   [Replicate updated Root Site](root-site-updated.md).
1. The system starts the process and follows UI-related steps analogous to
   what happens in [Replicate updated Root Site](root-site-updated.md).

## Alternate Course:
1. Author may choose to exclude some resources from replication cache cleansing.
1. Author may cancel the operation.
# Replicate new Page

**Actor**: Peregrine CMS Author  
**Description**: Author creates a new Resource and replicates it after an initial edit.

## Preconditions:
1. Resource does not exist in the system.

## Postconditions:
1. All selected resources associated with the Resource are now replicated.
1. The replication status icon gets updated.


## Main Course:
1. Author creates a new Resource.
1. Author edits the new Resource.
1. Author replicates the Resource.
1. System presents the author with a list of associated resources that
   will get replicated or updated along. The list presents the status change:
   whether the item will be updated in replication target or added or deleted.
    * System presents the resources that are referenced from our Resource.
    * System presents the resources that reference our Resource and will be affected.
1. Author confirms the operation.
1. The system analyzes the list of resources from the point of view
   of missing references analogously to how it is handled within
   [Replicate updated Root Site](root-site-updated.md).
1. The system starts the process and follows UI-related steps analogous to
   what happens in [Replicate updated Root Site](root-site-updated.md).

## Alternate Course:
1. Author may choose to exclude some associated resources from replication.
1. Author may cancel the operation.
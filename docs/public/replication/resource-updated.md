# Replicate updated page

**Actor**: Peregrine CMS Author  
**Description**: Author updates a Resource and replicates it.

## Preconditions:
1. Resource already exists in the system.

## Postconditions:
1. All selected resources associated with the Resource are now replicated.
1. The replication status icon gets updated.

## Main Course:
1. Author edits the Resource.
1. Author replicates the Resource.
1. System presents the author with a list of associated resources that
   will get replicated or updated along. The list presents the status change:
   whether the item will be updated in replication target or added or deleted.
    * System presents the resources that are referenced from our Resource.
    * System presents the resources that reference our Resource and will be affected.
1. Author confirms the operation.
1. The system performs the replication.

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.
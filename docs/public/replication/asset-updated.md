# Replicate updated Asset

**Actor**: Peregrine CMS Author  
**Description**: Author updates an Asset and replicates it.

## Preconditions:
1. Asset already exists in the system.

## Postconditions:
1. All tags associated with the Asset are now replicated.
1. The replication status icon gets updated.

## Main Course:
1. Author edits the Asset.
1. Author replicates the Asset.
1. System presents the author with a list of associated resources that
   will get replicated or updated along. The list presents the status change:
   whether the item will be updated in replication target or added or deleted.
1. Author confirms the operation, the system performs the replication.

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.
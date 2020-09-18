# Replicate new Asset

**Actor**: Peregrine CMS Author  
**Description**: Author creates a new Asset and replicates it after initial editing.

## Preconditions:
1. Asset does not exist in the system.

## Postconditions:
1. The tags referenced by the Asset get replicated.
1. The Asset gets replicated.
1. The replication status icon gets updated.

## Main Course:
1. Author creates a new Asset.
1. Author edits the new Asset.
1. Author replicates the Asset.
1. System presents the author with a list of associated resources that
   will get replicated or updated along.
1. Author confirms the operation, the system performs the replication.

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.
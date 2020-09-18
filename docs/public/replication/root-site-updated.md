# Replicate updated Root Site

**Actor**: Peregrine CMS Author  
**Description**: Author updates a website and replicates it.

## Preconditions:
1. Site already exists in the system.

## Postconditions:
1. All resources associated with the Site are now replicated.
1. The replication status icons reflect current state.

## Main Course:
1. Author edits resources of the Site.
1. Author initializes Site replication.
1. System presents the author with a list of associated resources that
   will get replicated or updated along. The list presents the status change:
   whether the item will be updated in replication target or added or deleted.
1. Author confirms the operation, the system performs the replication.

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.
# Replicate deleted Object

**Actor**: Peregrine CMS Author  
**Description**: Author deletes a Object.

## Preconditions:
1. Object already exists in the system.
1. Object is already replicated.

## Postconditions:
1. The Object is now removed from replication cache.

## Main Course:
1. Author deletes an Object.
1. System presents the author with a confirmation dialog asking about removal of
   replicated resources.
1. System presents the author a list of other pages that reference the Object to be removed.
1. Author confirms the operation, the system removes the replicated cache.

## Alternate Course:
1. Author may cancel the operation.
1. Author may access [replication cache explorer](./replication-explorer.md)
   to clean remaining resources.
1. Author may purge replication cache in [another console](./replication-explorer.md).

## Exceptions:
1. Object was not yet replicated. Nothing will happen in this case.
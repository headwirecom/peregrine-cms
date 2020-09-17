# Replicate deleted Asset

**Actor(s)**: Peregrine CMS Author  
**Description**: Author deletes an Asset.

## Preconditions:
1. Asset already exists in the system.
1. Asset is already replicated.

## Postconditions:
1. The Asset is now removed from replication cache.
1. All unreferenced tags associated with the Asset
   are now removed from replication cache.

## Main Course:
1. Author deletes an Asset.
1. System presents the author with a confirmation dialog asking about removal of
   replicated resources.
1. System presents the author a list of other pages that reference the Asset to be removed.
1. Author confirms the operation, the system removes the replicated cache.

## Alternate Course:
1. Author may choose to exclude some resources from replication cleansing.
1. Author may cancel the operation.
1. Author may access [replication cache explorer](./replication-explorer.md)
   to clean remaining resources.
1. Author may purge replication cache in [another console](./replication-explorer.md).

## Exceptions:
1. Asset was not yet replicated. Nothing will happen in this case.
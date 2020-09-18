# Replicate deleted template

**Actor**: Peregrine CMS Author  
**Description**: Author deletes a Template.

## Preconditions:
1. Template already exists in the system.
1. Template is already replicated.

## Postconditions:
1. The Template is now removed from replication cache.
1. All unreferenced resources associated with the Template
   are now removed from replication cache.

## Main Course:
1. Author deletes a Template.
1. System presents the author with a confirmation dialog asking about removal of
   replicated resources.
1. System presents the author a list of other pages that reference the Template to be removed.
1. Author confirms the operation, the system removes the replicated cache.

## Alternate Course:
1. Author may choose to exclude some resources from replication cleansing.
1. Author may cancel the operation.
1. Author may access [replication cache explorer](./replication-explorer.md)
   to clean remaining resources.
1. Author may purge replication cache in [another console](./replication-explorer.md).

## Exceptions:
1. Template was not yet replicated. Nothing will happen in this case.
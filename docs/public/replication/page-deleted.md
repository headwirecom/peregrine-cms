# Replicate deleted Page

**Actor(s)**: Peregrine CMS Author  
**Description**: Author deletes a Page.

## Preconditions:
1. Page already exists in the system.
1. Page is already replicated.

## Postconditions:
1. The Page is now removed from replication cache.
1. All unreferenced resources associated with the Page
   are now removed from replication cache.

## Main Course:
1. Author deletes a Page.
1. System presents the author with a confirmation dialog asking about removal of
   replicated resources.
1. System presents the author a list of other pages that reference the Page to be removed.
1. Author confirms the operation, the system removes the replicated cache.

## Alternate Course:
1. Author may choose to exclude some resources from replication cleansing.
1. Author may cancel the operation.
1. Author may access [replication cache explorer](./replication-explorer.md)
   to clean remaining resources.
1. Author may purge replication cache in [another console](./replication-explorer.md).

## Exceptions:
1. Page was not yet replicated. Nothing will happen in this case.
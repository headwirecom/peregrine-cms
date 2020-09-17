# Replicate deleted Root Site

**Actor(s)**: Peregrine CMS Author  
**Description**: Author deletes a Website.

## Preconditions:
1. Site already exists in the system.
1. Site is already replicated.

## Postconditions:
1. All resources associated with the Site are now removed from replication.

## Main Course:
1. Author deletes a Site.
1. System presents the author with a confirmation dialog asking about removal of
   replicated resources.
1. Author confirms the operation, the system removes the replicated cache.

## Alternate Course:
1. Author may choose to exclude some resources from replication cleansing.
1. Author may cancel the operation.
1. Author may access [replication cache explorer](./replication-explorer.md)
   to clean remaining resources.
1. Author may purge replication cache in [another console](./replication-explorer.md).

## Exceptions:
1. Site was not yet replicated. Nothing will happen in this case.
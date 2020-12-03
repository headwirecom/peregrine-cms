# Replicate deleted Root Site

**Actor**: Peregrine CMS Author  
**Description**: Author deletes and un-publishes a Website.

## Preconditions:
1. Site already exists in the system.
1. Site is already replicated.

## Postconditions:
1. All selected resources associated with the Site are now removed from replication cache.

## Main Course:
1. Author deletes a Site.
1. System presents the author with a confirmation dialog asking about removal of
   replicated resources.
1. Author confirms the operation.
1. The system removes the replicated cache by following the appropriate procedure from
   [Author un-publishes a Website](root-site-updated.md).

## Alternate Course:
1. Author may choose to exclude some resources from replication cleansing.
1. Author may cancel the operation.
1. Author may choose to delete the site from the system, but leave it in replication cache.
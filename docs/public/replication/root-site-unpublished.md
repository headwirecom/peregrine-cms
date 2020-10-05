# Un-publish Root Site

**Actor**: Peregrine CMS Author  
**Description**: Author un-publishes a Website.

## Preconditions:
1. Site already exists in the system.
1. Site is already replicated.

## Postconditions:
1. All selected resources associated with the Site are now removed from replication cache.

## Main Course:
1. Author un-publishes a Site.
1. System presents the author with a confirmation dialog asking about removal of
   replicated resources from replication cache.
1. Author confirms the operation.
1. The system analyzes the list of resources from the point of view
   of missing references analogously to how it is handled within
   [Replicate updated Root Site](root-site-updated.md).
1. The system starts the un-publishing process and follows UI-related steps analogous to
   what happens in [Replicate updated Root Site](root-site-updated.md).

## Alternate Course:
1. Author may choose to exclude some resources from replication cache cleansing.
1. Author may cancel the operation.
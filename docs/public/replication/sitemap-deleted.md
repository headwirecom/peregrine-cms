# Replicate deleted Sitemap

**Actors**: Peregrine CMS System, Peregrine CMS Author  
**Description**: Author disables the Site Map and System clears the replication cache.

## Preconditions:
1. Root Site exists in the system.
1. Site Map exists in the system and is already replicated.

## Postconditions:
1. The Site Map is no longer replicated in the cache.

## Main Course:
1. Author disables the Site Map with one of the following steps:
   * Remove `domains` property from `templates/jcr:content` node and replicate the change.
   * Unpublish the Site.
1. The System removes the Site Map from the replication cache.
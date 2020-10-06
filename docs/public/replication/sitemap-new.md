# Replicate new Sitemap

**Actors**: Peregrine CMS System, Peregrine CMS Author  
**Description**: System replicates a new Site Map.

## Preconditions:
1. Root Site exists in the system.
1. Site Map does not exist in the system.

## Postconditions:
1. The Site Map gets replicated into cache.

## Main Course:
1. Author enables Site Map with the following steps:
   * Add `domains` property to `templates/jcr:content` node.
   * Replicate the above node.
1. The System replicates the Site Map.
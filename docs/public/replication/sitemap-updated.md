# Replicate updated Sitemap

**Actors**: Peregrine CMS System, Peregrine CMS Author  
**Description**: Author updates a Site Map and System replicates it.

## Preconditions:
1. Root Site exists in the system.
1. Site Map exists in the system and is already replicated.

## Postconditions:
1. The Site Map gets replicated into cache.

## Main Course:
1. Author updates a part of the Site and replicates the change.
1. The System replicates the Site Map.
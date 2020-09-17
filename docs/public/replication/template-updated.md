# Replicate updated template

**Actor(s)**: Peregrine CMS Author  
**Description**: Author updates a Template and replicates it.

## Preconditions:
1. Template already exists in the system.

## Postconditions:
1. All resources associated with the Template are now replicated.
1. The replication status icon gets updated.

## Main Course:
1. Author edits the Template or its associated resources.
1. Author replicates the Template.
1. System presents the author with a list of associated resources that
   will get replicated or updated along. The list presents the status change:
   whether the item will be updated in replication target or added or deleted.
   It shows additionally the pages already built on top of the template.
1. Author confirms the operation, the system performs the replication.

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.
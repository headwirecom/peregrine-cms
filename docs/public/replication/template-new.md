# Replicate new template

**Actor**: Peregrine CMS Author  
**Description**: Author creates a new Template and replicates it after initial editing.

## Preconditions:
1. Template does not exist in the system.

## Postconditions:
1. The assets referenced by the Template get replicated.
1. The Template's super types get replicated.
1. The objects associated with the Template get replicated.
1. The replication status icon gets updated.

## Main Course:
1. Author creates a new Template.
1. Author adds and edits components to the new Template.
1. Author replicates the Template.
1. System presents the author with a list of associated resources that
   will get replicated or updated along.
1. Author confirms the operation, the system performs the replication.

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.
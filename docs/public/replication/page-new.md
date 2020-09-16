# Replicate new Page

**Actor(s)**: Peregrine CMS Author  
**Description**: Author creates a new page and replicates it after initial editing.

## Preconditions:
1. Page does not exist in the system.

## Postconditions:
1. The replication status icon gets updated.
1. The assets referenced by the page get replicated.
1. Other pages referenced by the page get replicated.
1. The page template and its super types get replicated.
1. The objects associated with the page get replicated.


## Main Course:
1. Author creates a new page.
1. Author adds and edits components to the new page.
1. Author replicates the page.
1. System presents the author with a list of associated resources that
   will get replicated or updated along.
1. Author confirms the operation, the system performs the replication.

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.

## Exceptions:
1. 
# Replicate updated Root Site

**Actor**: Peregrine CMS Author  
**Description**: Author updates a Website and replicates it.

## Preconditions:
1. Site already exists in the system.
1. Site was already replicated at least once (otherwise another use case applies:
   [Replicate new Root Site](root-site-new.md)).

## Postconditions:
1. All selected resources associated with the Site are now replicated.
1. All selected deleted resources are removed from the replication cache.
1. The replication status icons reflect current state.

## Main Course:
1. Author edits resources of the Site.
1. Author initializes Site replication.
1. System presents the author with a list of associated resources that
   will get replicated or updated along.
    1. The list presents the status change: whether the item will be updated
    in replication target or added or deleted.
    1. Author has the ability to select resources for activation.
1. Author confirms the operation.
1. The system analyzes the list of resources from the point of view
   of missing references analogously to how it is handled within
   [Replicate new Root Site](root-site-new.md) at the same step, but with one addition
   in case there are inconsistencies. I.e. a new inconsistency will occur if deleted resources
   are referenced from another, already replicated resource. Other than that this step is the same.
1. The system starts the replication and follows UI-related steps analogous to
   what happens in [Replicate new Root Site](root-site-new.md).

## Alternate Course:
1. Author may choose to exclude some resources from replication.
1. Author may cancel the operation.
# Replicate new Root Site

**Actor**: Peregrine CMS Author  
**Description**: Author creates a new Website
                 and replicates it after the initial preparations.

## Preconditions:
1. Site does not exist in the system under: **your websites > Websites**.
1. There is a theme to use already in the system.

## Postconditions:
1. All selected resources associated with the Site are now replicated.
1. The replication status icons reflect current state.

## Main Course:
1. Author creates a new Site.
1. Author edits associated resources that they want adjusted.
1. Author initializes Site Replication.
1. System presents the author with a list of associated resources that
   will get replicated or updated along.
1. Author confirms the operation.
1. The system analyzes the list of resources from the point of view of missing references.
    * If everything is consistent - nothing is shown.
    * If there are inconsistencies:
        * A referenced resource is not selected for replication while the referencing
         resource is,
        * or referenced paths point to resources that no longer exist in the system,   
      The system displays a warning with that information and suggests the best way
      to resolve these issues.
        1. Author confirms the operation.
            * Author can also choose to follow the suggested solution to deal with
              reference inconsistencies.
        1. The system displays another dialog to doubly confirm the risky operation.
        1. Author confirms the operation.
1. The system starts the replication.
   1. The view becomes blocked.
   1. The system presents the author with a progress bar.
   1. The system presents the author with a console panel where individual replicated paths
      are shown.
1. The system completes the process.
    1. A dialog informs the author of the result.

## Alternate Course:
* Author may choose to exclude some resources from replication.
* Author may cancel the operation in the confirmation dialog.
    1. No replication commences, the system state is not altered.
* The system might discover that even though the site is new:
    * There was a site of the same name in the past present in the system,
    * The previous site is still present in the replication cache.  
   The following course of actions takes place:
    1. The system displays a warning dialog that describes the problem
       and what will happen now.
    1. The user confirms the operation.
    1. The system displays another dialog stating that the next operation
       is not reversible.
    1. The user confirms the operation again.
    1. The system removes old replicated content completely from the given path.
    1. The system proceeds to replicate the site just as in the main course.
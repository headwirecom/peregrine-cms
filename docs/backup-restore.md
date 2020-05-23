# Requirement for Backup / Restore

This is a document to gather the requirements for [ticket 392](https://github.com/headwirecom/peregrine-cms/issues/392)

## Andy

In my opinion a full backup and restore (to restore a crashed server or to clone an existing server) should
only be allowed by an **admin**.

Tenant User can be allowed to do a Tenant Site backup but without any security information added.
During the restore-process the user then can add himself as admin to that tenant site.
If the tenant site already exists the user that does the restore must be part of that site.
The restore-process might require the user to either delete the existing site first or to restore them into
another site that does not exist (my-site -> my-site-2) and then the user has to manage it from there.

A Tenant based backup / restore require a dedicated process for it to deal with all these checks as the
Content-Package-based version does not handle permission and probably requires admin rights anyhow.


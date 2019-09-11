# Peregrine CMS Commons Package

This package contains commons code for the project.

**ATTENTION**: due to circular dependencies we cannot have tests in
this package even when they belong here. Please move all Tests from
this package to **commons-test**.

I know this is not optimal but I like to have a dedicated commons
test package to collect shared test code but w/o having to deal with
a second artifact created in **commons**. I am open to any suggestions
to fix this.

Andreas, 9/10/2019
 
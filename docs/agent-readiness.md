# Agent readiness

Peregrine's pitch is that it has **two interfaces to the same content**: a
visual editor for people and an HTTP/JSON interface for machines. Neither is a
bolt-on — the console itself is a client of the public API, and every node in
the repository answers as JSON.

What already works, and honestly differentiates:

- Everything the console does is a documented HTTP call (`/perapi/admin/*`);
  there is no private API.
- Every resource reads as JSON (`<path>.<depth>.json`, `<page>.data.json`).
- `/llms.txt` is served by the instance itself, in two versions: anonymous
  requests get an introduction that explains how to authenticate; an
  authenticated agent gets the full working guide **plus the list of sites its
  account can actually read**. The instance describes itself.
- The manual is generated from a passing end-to-end test run, so what the docs
  claim and what the product does cannot drift
  (https://preview.aemwip.com/api.html for the API chapters).
- Publishing is a version label, so "what a visitor sees" is a header an agent
  can send (`x-per-version-label: Published`) — the draft/live split is
  inspectable, not hidden infrastructure.

This document is the backlog for closing the rest of the gap. Ordered roughly
by value-for-effort within each group.

## 1. Making the machine interface first-class

**Component manifest in one call.** Today an agent needs `components.json`,
then `componentDefinition.json` per component, then a sample hunt to see real
usage. One endpoint that returns every component with its dialog schema, its
model properties and one sample node would collapse the most common agent
workflow (build a page from components it has never seen) into a single
request.

**OpenAPI description of `/perapi`.** The endpoints are stable and simple;
describing them in OpenAPI makes every SDK generator, request validator and
tool-use schema work out of the box. Serve it from the instance
(`/perapi/openapi.json`) so it can never lag the deployed version, and link it
from `/llms.txt`.

**Structured JSON errors.** Servlets today answer failures with a mix of HTML
error pages and ad-hoc JSON. A uniform envelope (`{status, code, message,
path, hint}`) means an agent can branch on `code` instead of parsing prose.
The `hint` field is where "did you mean `deep=true`?" lives.

**Markdown rendering for every page.** `<page>.md` alongside `.html` and
`.data.json`: the rendered content as plain markdown, which is the cheapest
tokens an LLM can read. With it, an `llms-sitemap` (every public page with its
`.md` URL) makes a whole site consumable in one crawl.

**Permission introspection.** `access.json` says who you are; it should also
say what you may do: readable site roots, writable paths, whether you can
publish. An agent that can ask "am I allowed to?" does not have to discover
permissions by failing.

**JCR-SQL2 recipes in the guide.** `/bin/search` is powerful and undiscoverable.
The authenticated `/llms.txt` should carry the five queries that answer 90% of
questions (pages by template, nodes referencing an asset, everything modified
since a date, unpublished pages, components in use).

## 2. Making writes safe enough to delegate

**Dry-run on every mutating endpoint.** A `dryRun=true` parameter (or
`X-Per-Dry-Run` header) that validates, reports what WOULD change, and touches
nothing. This is the single biggest trust feature: a person can approve an
agent's plan before it runs, and an agent can check its own work before
committing.

**Idempotent import with diff.** A documented "make this subtree look like
this JSON" endpoint that merges, reports created/updated/deleted, and can be
re-run safely. `updateResource` merges one node; agents build whole page
trees.

**Version-label diff.** "What differs between the draft and `Published`?"
as one endpoint, per subtree. It powers both a human review screen and an
agent's release note.

**Scoped API tokens.** Basic auth with the account password is all-or-nothing.
Tokens scoped to a site (or a path) with an expiry let a person hand an agent
exactly one site to work on — which is the trust boundary the collaboration
story needs.

**TTL scratch tenants.** `createTenant` with an expiry, for agents that want
to try something and show the result. Cheap because tenants are already
copy-on-create; the reaper is the only new part. (Related garbage-collection
fix that is already known: `deleteTenant` must also remove the site's user,
so names become reusable.)

## 3. Making the two interfaces work together

This is the part nobody else has: the person and the agent are not on separate
tracks — they meet on the same page node.

**Attribution on every write.** Writes already record `jcr:lastModifiedBy`;
surface it: the editor shows "drafted by agent-x 4 minutes ago" on a
component, the API returns it in `.data.json`. Trust starts with knowing who
did what.

**Change feed.** A `since=`-parameterized event endpoint (page created,
component edited, published, by whom) — the primitive under review queues
("show me everything the agent did today"), agent triggers ("re-check SEO when
a page changes"), and plain curiosity. A webhook is the push variant; the
pollable feed is the 80% version and much cheaper to build.

**Proposal pages.** An agent drafts on a copy (`createPageFromSkeletonPage`
already copies deep); a one-call "propose" marks it as pending review of page
X, the editor shows the diff, one click merges or discards. This turns the
dry-run and diff primitives into a human-approval workflow without inventing
a new storage model.

**Editor deep links in API responses.** Every write answer should carry
`editorUrl` (`/content/adminv2/pages/editor.html?path=…`). When an agent
reports "I created the page", the human's next action is one click, not a
path hunt.

**Comments as content.** A `per:Note` node an agent or person can attach to
any component ("this claim needs a source", "replaced the hero image —
check contrast"). Both interfaces render it; neither publishes it. The
conversation happens where the content is.

## Non-goals

- A separate "AI mode" or chat UI inside the console. The agent interface is
  HTTP; agents bring their own harness.
- MCP server as a hard dependency. An MCP server for Peregrine is worth
  shipping as a thin wrapper over `/perapi` + `/llms.txt`, but the HTTP
  interface stays the source of truth — MCP describes it, it does not replace
  it.
- Embedding-based site search. Nice, heavy, and orthogonal: agents that can
  read `.md` renditions and run JCR-SQL2 already answer retrieval questions.

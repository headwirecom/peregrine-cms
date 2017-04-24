# Repository Layout

### Layout

The following is the current layout of the repository

```shell
├───apps
├───content
│   ├───admin
│   ├───articles
│   ├───assets
│   └───sites
└───etc
    └───felibs
```

Each type of asset is stored in an individual sub folder of `/content`. The idea behind 
this is to make sure we can easily separate the repository into multiple instances in
the future. 

The `/apps` folder should contain the different parts of peregerine-cms (potentially to 
be moved to `/libs` at a later time). 

We try to subscribe to the mantra ***everything is content***. As such, our administration 
user interface is content as well and is basically set up like any regular website built
with PER:CMS. 

`/etc/felibs` is used to store the front end libraries (sub folder for your library, 
`per:FeLib` as `jcr:resourceType`)

### Notes

- We currently develop under `/apps`, probably have to move this to `/libs`
- Not sure I like `/content/articles` (eg is it really articles, fragments? better name?)

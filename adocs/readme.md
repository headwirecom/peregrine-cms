adocs
=====

A great tool needs great documentation. A(utomatic)DOCS is an effort to create some of the documentation
automatically. Taking screenshots from the product for example should be easy to achieve and that's why
we started this project in the first place. 

Note: you will need node7 to run these scripts

### run
run chrome with remote debugging enabled on port 9992
```
"c:\Program Files (x86)\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222 http://localhost:8080
```

create the screenshots
```
makeall.bat
```

create an index.html file

```
node report.js > index.html
```

this will generate  target folder and an `index.html` file in the root. The index.html file contains
a set of screenshots from the product with a set of resolutions (400, 600, ,800,1024,1280)

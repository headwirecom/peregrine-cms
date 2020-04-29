Feature('demo flow');

function write(target, text) {
    for(let i = 0; i < text.length; i++) {
        target.pressKey(text.charAt(i))
    }
}

const username = 'admin'
const password = 'admin'

const token = Buffer.from(`${username}:${password}`, 'utf8').toString('base64')

Scenario('demo flow simple site', async (I) => {

    I.amOnPage('/')
    I.say("let's login to peregrine")
    I.seeInTitle('Login')
    I.fillField('Username', 'admin')
    I.fillField('Password', 'admin')
    I.click('Log In')
    I.waitForNavigation()
    const response = await I.sendPostRequest('/admin/deleteTenant.json?name=sunshine', {}, {
        'Authorization': `Basic ${token}`
      })
    I.seeInTitle('Welcome')
    I.switchToPreviousTab(0)
    I.say("we can create a new website by pressing the 'create new site' button")
    I.click(locate('div.create-tenant.action').as('Create new website'))
    I.seeInTitle('Create Site')
    I.say("choose a template, we'll choose thenecleanflex as it's tailwind and flex box based")
    I.click('themecleanflex')
    I.click('Next')
    I.say("we can choose a color scheme")
    I.click('Next')
    I.say("and let's enter our site name")
    I.fillField('Site Title', 'sunshine')
    I.click('Next')
    I.say("validate this is what you'd like to do and click finish to create your site")
    I.click('Finish')
    I.switchToPreviousTab(0)
    I.seeInTitle('Home')
    I.say("We're now on our website home screen. Let's click the asset card or assets in the top navigation")
    I.click('Assets')
    I.switchToPreviousTab(0)
    I.seeInTitle('Assets')
    I.say("We can upload an image from our computer or source one through pixabay")
    I.click('get asset from pixabay')
    I.say("let's find a malinois and save it to our local insance")
    I.fillField('Search for an image asset', 'malinois')
    I.click('search')
    I.click(locate('div.image-item.hoverable'))
    I.fillField(locate('input').at(2), 'example.png')
    I.click('save')
    I.switchToPreviousTab(0)
    I.click('example.png')
    I.wait(2)
    I.switchToPreviousTab(0)    
    I.say("we have an asset now, so let's start building a page - we click on pages in the menu and see the page hierarchy")
    I.click('Pages')
    I.switchToPreviousTab(0)
    I.seeInTitle('Pages')
    I.say("we can open the index page")
    I.click('index')
    I.switchToPreviousTab(0)
    I.seeInTitle('Page Editor')

    I.say("let's drag a component from the right side to the page and fill in some content")
    const addRichtext = await I.sendPostRequest('/admin/insertNodeAt.json/content/sunshine/pages/index/jcr:content/main?component=/apps/sunshine/components/richtext&drop=into-before&variation=sample-media', 
        {},{ 'Authorization': `Basic ${token}`}
    )
    I.switchToPreviousTab(0)
    I.refreshPage()
    I.wait(2)
    I.switchToPreviousTab(0)
    I.click('#editviewoverlay')
    I.fillField('.trumbowyg-editor', '')
    I.pressKey(['Control', 'A'])
    I.pressKey('Backspace')
    write(I, "Hey") // , this is kind of cool, isn't it?")

    I.fillField('#image-source', '/content/sunshine/assets/example.png')

    I.click(locate('button').withAttr({ title: 'save'}).as('save'))


    I.say("of course we also want to do a teaser at the top of the page")

    const addTeaser = await I.sendPostRequest('/admin/insertNodeAt.json/content/sunshine/pages/index/jcr:content/main?component=/apps/sunshine/components/teaserhorizontal&drop=into-before&variation=sample', 
        {},{ 'Authorization': `Basic ${token}`}
    )

    I.refreshPage()
    I.wait(2)
    I.switchToPreviousTab(0)

    I.click('Pages')
    I.switchToPreviousTab(0)
    I.seeInTitle('Pages')
    I.switchToPreviousTab(0)

    I.click("view 'index' in new tab")
    I.switchToNextTab(1)


});

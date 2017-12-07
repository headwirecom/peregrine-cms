class AddObjectWizard {
  get container() { return browser.element('.vue-form-wizard')}
  get templates() {
      const items = $$('.wizard-tab-container > ul > li')
      return items.map(item => {
          return new Item(item);
      })
  } 
}

class Item {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$(`span > a`).getText()}
    get linkButton()    { return this.container.$(`span > a`) }
    get classAttribute() {return this.container.getAttribute('class')}
}

module.exports = AddObjectWizard
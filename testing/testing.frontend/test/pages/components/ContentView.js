class ContentView {
    get container() {return $('.peregrine-content-view')}
    get perApp() {
        browser.frame($('#editview'));
        let app = $('.peregrine-app')
        console.log(app.getText());
        browser.frame(null);
        return app;
    } 
}

module.exports = ContentView;
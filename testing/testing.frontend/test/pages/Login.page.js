var Page = require('./page')
class LoginPage extends Page {
    get username()  { return browser.element('#j_username'); }
    get password()  { return browser.element('#j_password'); }
    get login()      { return browser.element('#login'); }
    get err()       { return browser.element('#err'); }
    
    open(path) {
        super.open(path);
        if( browser.getUrl().search('system/sling/form/login') > -1 ){
            this.username.setValue('admin');
            this.password.setValue('admin');
            this.login.click();
        }
    }
    
    submit() {
        this.form.submitForm();
    }
    
}
module.exports = LoginPage
let objectsPage = require('../pages/Objects.page')
let {Explorer, SubNav, AddObjectWizard, ObjectEditorPanel} = objectsPage

describe('Peregrine objects page', function () {
    it('should login', function() {
    	objectsPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/objects.html')
    })

    describe('Objects Explorer', function() {
        let exampleFolder
        
        it('should have a folder titled "example"', function(){
        	Explorer.container.waitForVisible()
            const folders = Explorer.folders
            const i = folders.findIndex( folder => folder.text.indexOf('example') > -1 ) 
            exampleFolder = folders[i]
            expect( exampleFolder.text ).to.contain('example')
        })
        
        it('clicking example folder item should load all objects', function(){
        	exampleFolder.linkButton.click()
        	expect( browser.getUrl() ).to.contain('content/objects/example' )
        }) 
        
        let exampleObject
        
        it('should have an object titled "sample"', function(){
        	Explorer.container.waitForVisible()
            const objects = Explorer.objects
            const i = objects.findIndex( object => object.text.indexOf('sample') > -1 ) 
            exampleObject = objects[i]
            expect( exampleObject.text ).to.contain('sample')
        })

    })
    
    describe('Add a new object', function() {
    	it('should see the subnav', function() {
        	SubNav.container.waitForVisible()
        	expect( SubNav.container.isVisible() ).to.equal(true)
        })

        it('clicking add object should navigate to add object wizard', function() {
            SubNav.addObjectButton.click()
            AddObjectWizard.container.waitForVisible()
            expect( browser.getUrl() ).to.contain('content/admin/pages/objects/create.html/path:/content/example/objects')
        })
        
        let exampleTemplate
        
        it('should see sample in template list', function() {
            AddObjectWizard.container.waitForVisible()
            const templates = AddObjectWizard.templates
            const i = templates.findIndex( template => template.text.indexOf('sample') > -1 ) 
            exampleTemplate = templates[i]
            expect( exampleTemplate.text ).to.contain('sample')
        })
        
        it('selecting sample template', function() {
        	exampleTemplate.linkButton.click()
            expect( exampleTemplate.classAttribute ).to.contain('grey lighten-2')
        })
        
        it('clicking next button should go wizard step 2', function() {
            AddObjectWizard.nextButton.click()
            AddObjectWizard.objectNameField.waitForVisible()
            expect( AddObjectWizard.objectNameField.isVisible() ).to.equal(true)
        })
        
        it('clicking next button should go wizard step 3', function() {
            AddObjectWizard.objectNameField.setValue('myobject1')
            AddObjectWizard.nextButton.click()
            expect( AddObjectWizard.finishButton.isVisible() ).to.equal(true)
        })
        
       let newObject
        
       it('clicking finish button to add this object', function() {
        	AddObjectWizard.finishButton.scroll(0,10000)
            AddObjectWizard.finishButton.click()
            Explorer.container.waitForVisible()
            const objects = Explorer.objects
            const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 ) 
            newObject = objects[i]
            expect( newObject.text ).to.contain('myobject1')
        })

    })
    
    describe('Edit an object', function() {
    	
	
    	let exampleObject
    	
    	it('find object to edit', function() {
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.not.equal(-1)
	        exampleObject = objects[i]
	    })
    	
    	it('clicking edit item should load the object editor panel', function(){
    		exampleObject.editButton.click()
    		ObjectEditorPanel.container.waitForVisible()
            expect( ObjectEditorPanel.container.isVisible() ).to.equal(true)
        })
    	
    	
        let inputs
        let textEditor
        
        it('editing text field', function(){
    		inputs = ObjectEditorPanel.inputs
    		inputs[0].setValue('first text')
    		expect( inputs[0].getValue() ).to.equal('first text')
    	})
    	
    	it('editing textarea', function(){
    		inputs[1].setValue('first textarea')
    		expect( inputs[1].getValue() ).to.equal('first textarea')
    	})
        
        it('editing text editor', function(){
        	textEditor = ObjectEditorPanel.textEditor
        	textEditor.setValue('first text editor')
        	expect( textEditor.getText() ).to.equal('first text editor')
    	})
        
        it('editing number field', function(){
    		inputs[3].setValue('125')
    		expect( inputs[3].getValue() ).to.equal('125')
    	})
    	
    	it('editing url field', function(){
    		inputs[4].setValue('http://www.peregrine-cms.com')
    		expect( inputs[4].getValue() ).to.equal('http://www.peregrine-cms.com')
    	})
    	
    	it('editing tel field', function(){
    		inputs[5].setValue('9496009999')
    		expect( inputs[5].getValue() ).to.equal('9496009999')
    	})
    	
    	// page path browser field testing
    	let pathBrowserButtons
    	let pathBrowserContainer
    	
    	it('open page path browser', function(){
    		pathBrowserButtons = ObjectEditorPanel.pathBrowserButtons	
    		pathBrowserContainer = ObjectEditorPanel.pathBrowserContainer
    		
    		// open up modal container
    		pathBrowserButtons[0].click()
    		pathBrowserContainer.waitForVisible()
    		expect( pathBrowserContainer.isVisible() ).to.equal(true)
		})
    	
    	let exampleSite
    	
    	it('should have a page named example in path browser', function(){
    		
    		const sites = ObjectEditorPanel.sites
            const i = sites.findIndex( site => site.text.indexOf('example') > -1 ) 
            exampleSite = sites[i]
            expect( exampleSite.text ).to.contain('example')
    		
    	})
    	
    	it('select page named example', function(){
    		
    		exampleSite.label.click()
    		expect( ObjectEditorPanel.selectedPath ).to.contain('/content/example/pages')
    		ObjectEditorPanel.selectPathButton.click()
    		// wait for 1 second till the animation ends
    		browser.pause(1000)
    	})
    	
    	// asset path browser field testing
    	it('open asset path browser', function(){
    		   		
    		// open up modal container
    		pathBrowserButtons[1].click()
    		pathBrowserContainer.waitForVisible()
    		expect( pathBrowserContainer.isVisible() ).to.equal(true)
		})
		
		let exampleAsset
    	
    	it('should have an asset named peregrine-logo.png in path browser', function(){
    		
    		const sites = ObjectEditorPanel.sites
            const i = sites.findIndex( site => site.text.indexOf('peregrine-logo.png') > -1 ) 
            exampleAsset = sites[i]
            expect( exampleAsset.text ).to.contain('peregrine-logo.png')
    		
    	})
    	
        it('select asset named peregrine-logo.png', function(){
    		
    		exampleAsset.label.click()
    		expect( ObjectEditorPanel.selectedPath ).to.contain('/content/example/assets/images/peregrine-logo.png')
    		ObjectEditorPanel.selectPathButton.click()
    		// wait for 1 second till the animation ends
    		browser.pause(1000)
    	})
    	
    	it('selecting checkbox field', function(){
    		ObjectEditorPanel.checkboxLabel.click()
    		expect( ObjectEditorPanel.checkboxInput ).to.equal('on')
    	})
    	
    	// radio button testing
    	let exampleRadioButton
    	
    	it('should have a radio button with value left', function(){
    		const buttons = ObjectEditorPanel.radioButtons
    		const i = buttons.findIndex( button => button.text.indexOf('left') > -1 ) 
            exampleRadioButton = buttons[i]
            expect( exampleRadioButton.text ).to.contain('left')
    	})
    	
    	it('selecting left from radio button list', function(){
    		exampleRadioButton.label.click()
    		expect( exampleRadioButton.container.getAttribute('class') ).to.equal('checked')
    	})
    	
    	// material switch field testing
    	it('setting materialswitch field to yes', function(){
    		ObjectEditorPanel.switchLabel.click()
    		expect( ObjectEditorPanel.switchLabel.getText() ).to.equal('yes')
    	})
    	
    	// range field testing
    	it('setting range field to 800', function(){
    		ObjectEditorPanel.rangeInput.selectorExecute( function(input){$( input ).val(800) })
    		expect( ObjectEditorPanel.rangeInput.getValue() ).to.equal('800')
    	})
    	
    	// multiselect field testing
    	let exampleMultiSelect
    	let exampleOption
    	
    	it('should have an option Type 2 from Multi Select Field', function(){
    		exampleMultiSelect = ObjectEditorPanel.multiSelect;
    		exampleMultiSelect.selectButton.click()
    		exampleMultiSelect.contentWrapper.waitForVisible()
    		const options = exampleMultiSelect.items
    		const i = options.findIndex( option => option.text.indexOf('Type 2') > -1 ) 
    		exampleOption = options[i]
    		expect( exampleOption.text ).to.contain('Type 2')
    		
    	})
    	
    	let exampleTag
    	it('selecting Type 2 from Multi Select Field', function(){
    		exampleTag = exampleMultiSelect.tag
    		exampleOption.span.click()
    		exampleTag.waitForVisible()
    		expect( exampleTag.getText() ).to.equal('Type 2')
    		//ObjectEditorPanel.save.click()
    	})
    	
    	// time field testing
    	let exampleTimeInput
    	let exampleTimeModal
    	it('clicking input field should open up time picker modal', function(){
    		exampleTimeInput = ObjectEditorPanel.timeInput
    		exampleTimeInput.click()
    		exampleTimeModal = ObjectEditorPanel.datetimeBrowserContainer
    		exampleTimeModal.waitForVisible()
    		expect( exampleTimeModal.isVisible() ).to.equal(true)
    	})
    	
    	let exampleHour
    	it('should have an hour select with value 08', function(){
    		const hours = ObjectEditorPanel.hours
    		const i = hours.findIndex( hour => hour.text.indexOf('08') > -1 ) 
            exampleHour = hours[i]
            expect( exampleHour.text ).to.contain('08')
        })
    	
    	let exampleMinute
    	it('should have a minute select with value 30', function(){
    		const minutes = ObjectEditorPanel.minutes
    		const i = minutes.findIndex( minute => minute.text.indexOf('30') > -1 ) 
            exampleMinute = minutes[i]
            expect( exampleMinute.text ).to.contain('30')
    	})
    	
    	it('selecting 08:30 in time picker modal', function(){
    		exampleHour.div.click()
    		exampleMinute.div.click()
    		ObjectEditorPanel.datetimeSaveButton.click()
    		// wait for animation to end
    		browser.pause(1000)
    		exampleTimeInput.waitForVisible()
    		expect( exampleTimeInput.getValue() ).to.contain('08:30')
    		ObjectEditorPanel.save.waitForVisible()
    	})
    	
    	// date field testing
    	let exampleDateInput
    	let exampleDateModal
    	it('clicking input field should open up date picker modal', function(){
    		exampleDateInput = ObjectEditorPanel.dateInput
    		exampleDateInput.click()
    		exampleDateModal = ObjectEditorPanel.datetimeBrowserContainer
    		exampleDateModal.waitForVisible()
    		expect( exampleDateModal.isVisible() ).to.equal(true)
    	})
    	
    	
    	let exampleDay
    	let exampleMonthSelector
    	it('should have a day of 15 in previous month', function(){
    		exampleMonthSelector = ObjectEditorPanel.monthSelector
    		exampleMonthSelector.previousButton.click()
    		const days = ObjectEditorPanel.days
    		const i = days.findIndex( day => day.text.indexOf('15') > -1 ) 
            exampleDay = days[i]
            expect( exampleDay.text ).to.contain('15')
        })
    	
    	let dateString
    	it('selecting day 15 of previous month in date picker modal', function(){
    		exampleDay.div.click()
    		dateString = getDateString(exampleMonthSelector.currentMonthYear,'15')
    		ObjectEditorPanel.datetimeSaveButton.click()
    		// wait for animation to end
    		browser.pause(1000)
    		exampleDateInput.waitForVisible()
    		expect( exampleDateInput.getValue() ).to.contain(dateString)
    		//ObjectEditorPanel.save.waitForVisible()
    		//ObjectEditorPanel.save.click()
    	})
    	
    	// datetime field testing
    	let datetimeInput
    	let dateModal
    	it('clicking input field should open up datetime picker modal', function(){
    		datetimeInput = ObjectEditorPanel.datetimeInput
    		datetimeInput.click()
    		dateModal = ObjectEditorPanel.datetimeBrowserContainer
    		dateModal.waitForVisible()
    		expect( dateModal.isVisible() ).to.equal(true)
    	})
    	
    	let day
    	let monthSelector
    	it('should have a day of 20 in next month', function(){
    		monthSelector = ObjectEditorPanel.monthSelector
    		monthSelector.nextButton.click()
    		const days = ObjectEditorPanel.days
    		const i = days.findIndex( day => day.text.indexOf('20') > -1 ) 
            day = days[i]
            expect( day.text ).to.contain('20')
        })
    	
    	let datetimeString
    	it('selecting day 20 of next month in date picker modal', function(){
    		day.div.click()
    		datetimeString = getDateString(monthSelector.currentMonthYear,'20')
    		ObjectEditorPanel.datetimeSaveButton.click()
    		// wait for animation to end
    		browser.pause(1000)
    	})
    	
    	let timeModal
    	it('expecting time picker modal', function(){
    		timeModal = ObjectEditorPanel.datetimeBrowserContainer
    		timeModal.waitForVisible()
    		expect( timeModal.isVisible() ).to.equal(true)
    	})
    	
    	let hour
    	it('should have an hour select with value 12', function(){
    		const hours = ObjectEditorPanel.hours
    		const i = hours.findIndex( hour => hour.text.indexOf('12') > -1 ) 
            hour = hours[i]
            expect( hour.text ).to.contain('12')
        })
    	
    	let minute
    	it('should have a minute select with value 45', function(){
    		const minutes = ObjectEditorPanel.minutes
    		const i = minutes.findIndex( minute => minute.text.indexOf('45') > -1 ) 
            minute = minutes[i]
            expect( minute.text ).to.contain('45')
    	})
    	
    	it('selecting 12:45 in time picker modal', function(){
    		hour.div.click()
    		minute.div.click()
    		ObjectEditorPanel.datetimeSaveButton.click()
    		// wait for animation to end
    		browser.pause(1000)
    		datetimeInput.waitForVisible()
    		expect( datetimeInput.getValue() ).to.contain(datetimeString + ' 12:45')
    	})
    	
    	let exampleCollection
    	it('clicking add button should add a collection item', function(){
    		exampleCollection = ObjectEditorPanel.collection
    		exampleCollection.addButton.click()
    		//exampleCollection.items.waitForVisible()
    		expect( exampleCollection.items.length ).to.equal(1)
    	})
    	
    	it('clicking delete button should remove a collection item', function(){
    		exampleCollection.addButton.click()
    		expect( exampleCollection.items.length ).to.equal(2)
    		// now delete
    		exampleCollection.items[1].deleteButton.waitForVisible()
    		exampleCollection.items[1].deleteButton.click()
    		expect( exampleCollection.items.length ).to.equal(1)
    	})
    	
    	let exampleCollectionItem
    	let browserContainer
    	let asset
    	it('authoring collection item', function(){
    		exampleCollectionItem = exampleCollection.items[0]
    		exampleCollectionItem.header.waitForVisible()
    		exampleCollectionItem.header.click()
    		browserContainer = ObjectEditorPanel.pathBrowserContainer
    		exampleCollectionItem.title.setValue('collection title')
    		expect( exampleCollectionItem.title.getValue() ).to.equal('collection title')
    		exampleCollectionItem.text.setValue('collection text')
    		expect( exampleCollectionItem.text.getText() ).to.equal('collection text')
    		exampleCollectionItem.imageButton.click()
    		browserContainer.waitForVisible()
    		const sites = ObjectEditorPanel.sites
            const i = sites.findIndex( site => site.text.indexOf('peregrine-logo.png') > -1 ) 
            asset = sites[i]
    		asset.label.click()
    		expect( ObjectEditorPanel.selectedPath ).to.contain('/content/example/assets/peregrine-logo.png')
    		ObjectEditorPanel.selectPathButton.click()
    		// wait for 1 second till the animation ends
    		browser.pause(1000)
    	})
    	
    	let exampleColor
    	it('clicking color button should open up color picker window', function(){
    		exampleColor = ObjectEditorPanel.color
    		exampleColor.button.click()
    	})
    	
    	it('setting color green on color field', function(){
    		exampleColor.button.selectorExecute( function(input){$( input ).val('#008040') })         
    		//exampleColor.colorCode.text('#008040')
    		expect( exampleColor.button.getValue() ).to.equal('#008040')
    		//expect( exampleColor.colorCode.getText() ).to.equal('#008040')
    		
    	})
    	
    	let exampleIconBrowser
    	let exampleIconBrowserModal
    	it('clicking Icon browser button should open up Icon Browser Modal', function(){
    		exampleIconBrowser = ObjectEditorPanel.iconBrowser
    		exampleIconBrowser.button.click()
    		exampleIconBrowserModal = ObjectEditorPanel.iconBrowserContainer
    		exampleIconBrowserModal.waitForVisible()
    		expect( exampleIconBrowserModal.isVisible() ).to.equal(true)
    	})
    	
        let exampleIcon
    	
    	it('should have an icon with name add_shopping_cart', function(){
    		
    		const icons = ObjectEditorPanel.icons
            const i = icons.findIndex( icon => icon.text.indexOf('add_shopping_cart') > -1 ) 
            exampleIcon = icons[i]
            expect( exampleIcon.text ).to.contain('add_shopping_cart')
        })
    	
    	it('select shopping cart icon', function(){
    		exampleIcon.label.click()
    		expect( ObjectEditorPanel.selectedIcon ).to.contain('add_shopping_cart')
    		ObjectEditorPanel.selectIconButton.click()
    		// wait for 1 second till the animation ends
    		browser.pause(1000)
    		ObjectEditorPanel.save.click()
    	})
    	
    	    	    	    	    	
    	let previewContainer
    	
    	it('text field saved', function(){
    		previewContainer = ObjectEditorPanel.previewContainer
    		expect( previewContainer.$('.form-group:nth-child(1) > .field-wrap > .wrapper > p').getText() ).to.equal('first text')
    	})
    	
    	it('textarea field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(2) > .field-wrap > .wrapper > p').getText() ).to.equal('first textarea')
    	})
    	
    	it('text editor saved', function(){ 
    		//previewContainer = ObjectEditorPanel.previewContainer
    		expect( previewContainer.$('.form-group:nth-child(3) > .field-wrap > div > p').getText() ).to.equal('first text editor')
    	})
    	
    	it('number field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(4) > .field-wrap > .wrapper > p').getText() ).to.equal('125')
    	})
    	
    	it('url field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(5) > .field-wrap > .wrapper > p').getText() ).to.equal('http://www.peregrine-cms.com')
    	})
    	
    	it('tel field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(6) > .field-wrap > .wrapper > p').getText() ).to.equal('9496009999')
    	})
    	
    	it('site path browser field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(7) > .field-wrap > .wrap > p').getText() ).to.equal('/content/example/pages')
    	})
    	
    	it('asset path browser field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(8) > .field-wrap > .wrap > p').getText() ).to.equal('/content/example/assets/images/peregrine-logo.png')
    	})
    	
    	it('checkbox field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(9) > .field-wrap > .wrap > p').getText() ).to.equal('true')
    	})
    	
    	it('radio button field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(10) > .field-wrap > .wrap > p').getText() ).to.equal('Left')
    	})
    	
    	it('material switch field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(11) > .field-wrap > .wrap > p').getText() ).to.equal('true')
    	})
    	/*
    	it('range field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(12) > .field-wrap > .wrapper > span > span').getText() ).to.equal('800')
    	})*/
    	
    	it('multi select field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(13) > .field-wrap > .wrap .multiselect__single').getText() ).to.equal('Type 2')
    	})
    	it('time field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(14) > .field-wrap > .vdatetime > input').getValue() ).to.equal('08:30')
    	})
    	it('date field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(15) > .field-wrap > .vdatetime > input').getValue() ).to.equal(dateString)
    	})
    	it('datetime field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(16) > .field-wrap > .vdatetime > input').getValue() ).to.equal(datetimeString + ' 12:45')
    	})
    	it('collection field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(17) > .field-wrap > .wrap .field-input p').getText() ).to.equal('collection title')
    		expect( previewContainer.$('.form-group:nth-child(17) > .field-wrap > .wrap .field-texteditor p').getText() ).to.equal('collection text')
    		expect( previewContainer.$('.form-group:nth-child(17) > .field-wrap > .wrap .field-pathbrowser p').getText() ).to.equal('/content/example/assets/peregrine-logo.png')
    	})
    	it('icon browser field saved', function(){   
    		expect( previewContainer.$('.form-group:nth-child(19) > .field-wrap > .wrap p').getText() ).to.equal('add_shopping_cartmaterial:material-icons:add_shopping_cart')
    	})
    })
    
    describe('Delete an object', function() {
    	
    	let exampleObject
    	
    	it('find object to be deleted', function() {
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.not.equal(-1)
	        exampleObject = objects[i]
	    })
    	
    	it('delete object', function() {
    		exampleObject.deleteButton.click()
    		browser.alertAccept();
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.equal(-1)
	    })
    })
    
    function getDateString(monthYear, day) {
		var months = [
		    'January',
		    'February',
		    'March',
		    'April',
		    'May',
		    'June',
		    'July',
		    'August',
		    'September',
		    'October',
		    'November',
		    'December'
		  ];
	      var Value = monthYear.split(" ")      
		  var month = (months.indexOf(Value[0]) + 1)
		  if(month < '10'){
			  month = '0' + month
		  }
		  var year = Value[1]
	      return year + '-' + month + '-' + day
    }
})


package com.example.site.models;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import java.util.List;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;


@Model(
        adaptables = Resource.class,
        resourceType = "example/objects/sample",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(name = JACKSON,
          extensions = JSON)
public class SampleModel extends AbstractComponent {


    public SampleModel(Resource r) { super(r); }

    @Inject
    private String text;

    @Inject
    private String textarea;
    
    @Inject
    private String texteditor;
    
    @Inject
    private String number;
    
    @Inject
    private String tel;
    
    @Inject
    private String url;

    @Inject
    private String pagePath;

    @Inject
    private String imagePath;
    
    @Inject
    private String checkbox;
    
    @Inject
    private String radiobutton;
    
    @Inject
    private String materialswitch;
    
    @Inject
    private String range;
    
    @Inject
	  private String select;
    
    @Inject
	  private String time;

	  @Inject
	  private String date;
	
	  @Inject
	  private String datetime;
	
	  @Inject
	  private List<IComponent> cards;
	
	  @Inject
	  private String color;
	
	  @Inject
	  private String icon;

    public String getText() {
        return text;
    }

    public String getTextarea() {
        return textarea;
    }
    
    public String getTexteditor() {
        return texteditor;
    }
    
    public String getNumber() {
        return number;
    }
    
    public String getTel() {
        return tel;
    }
    
    public String getUrl() {
        return url;
    }

    public String getPagePath() {
        return pagePath;
    }

    public String getImagePath() {
        return imagePath;
    }
    
    public String getCheckbox() {
        return checkbox;
    }
    
    public String getRadiobutton() {
        return radiobutton;
    }
    
    public String getMaterialswitch() {
        return materialswitch;
    }
    
    public String getRange() {
        return range;
    }
    
    public String getSelect() {
		return select;
	}
    
    public String getTime() {
		return time;
	}

	public String getDate() {
		return date;
	}
	
	public String getDatetime() {
		return datetime;
	}
	
	public List<IComponent> getCards() {
		return cards;
	}
	
	public String getColor() {
		return color;
	}
	
	public String getIcon() {
		return icon;
	}
}

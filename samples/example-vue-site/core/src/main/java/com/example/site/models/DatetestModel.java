package com.example.site.models;

import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/*
    //GEN[:DATA
    {
  "definitions": {
    "Entry": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "project": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Project",
          "x-form-type": "materialselect",
          "x-form-from": "/content/example/objects/timetracker/projects.infinity.json"
        },
        "description": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Description",
          "x-form-type": "text"
        },
        "start": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "Start",
          "x-form-type": "materialdatetime"
        },
        "end": {
          "type": "string",
          "x-source": "inject",
          "x-form-label": "End",
          "x-form-type": "materialdatetime"
        }
      }
    }
  },
  "name": "Entry",
  "componentPath": "timetracker/objects/datetest",
  "package": "com.timetracker.models",
  "modelName": "Entry",
  "classNameParent": "AbstractComponent"
}
//GEN]
*/

//GEN[:DEF
@Model(
        adaptables = Resource.class,
        resourceType = "example/objects/datetest",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)

//GEN]
public class DatetestModel extends AbstractComponent {

    public DatetestModel(Resource r) { super(r); }

	/* {"type":"string","x-source":"inject","x-form-label":"Description","x-form-type":"text"} */
	@Inject
	private String description;

	/* {"type":"string","x-source":"inject","x-form-label":"Start","x-form-type":"materialdatetime"} */
	@Inject
	private String start;

	/* {"type":"string","x-source":"inject","x-form-label":"End","x-form-type":"materialdatetime"} */
	@Inject
	private String end;


//GEN]

	/* {"type":"string","x-source":"inject","x-form-label":"Description","x-form-type":"text"} */
	public String getDescription() {
		return description;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"Start","x-form-type":"materialdatetime"} */
	public String getStart() {
		return start;
	}

	/* {"type":"string","x-source":"inject","x-form-label":"End","x-form-type":"materialdatetime"} */
	public String getEnd() {
		return end;
	}


//GEN]

    //GEN[:CUSTOMGETTERS
    //GEN]

}

package com.peregrine.nodejs.npm;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Created by Andreas Schaefer on 4/4/17.
 */
@ObjectClassDefinition(
    name = "NPM Process",
    description = "The NPM Process to list, install and remove Node.js packages"
)
@interface NpmExternalProcessConfiguration {

    @AttributeDefinition(
        name = "NPM Repository Folder",
        description = "The absolute Path ot the NPM Repository used install / retrieve the Node.js packages"
    )
    String npmRepositoryFolderPath() default ".";
}

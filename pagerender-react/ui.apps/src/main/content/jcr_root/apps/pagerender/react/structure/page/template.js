import React from 'react'

const component = ({model}) => (
    <div>
        {model.children.map( function(child, i) {
            const Component = window[$peregrineApp.componentNameToVar(child.component)]
            return <Component key={child.path} model={child}/>
        })}
    </div>
)

export default component

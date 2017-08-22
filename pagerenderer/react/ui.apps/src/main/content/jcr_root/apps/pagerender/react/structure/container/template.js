import React from 'react'

const component = ({model}) => (
    <div data-per-path={model.path}>
        {model.children.map( function(child, i) {
            const Component = window[$peregrineApp.componentNameToVar(child.component)]

            return <Component key={child.path} model={child}/>
        })}
    </div>
)

export default component

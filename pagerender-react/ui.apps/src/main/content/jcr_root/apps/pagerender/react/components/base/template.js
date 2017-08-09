import React from 'react'

function getText(model) {

    return {__html: model.text}

}

const component = ({model}) => (
    <div dangerouslySetInnerHTML={getText(model)} data-per-path={model.path}></div>
)

export default component

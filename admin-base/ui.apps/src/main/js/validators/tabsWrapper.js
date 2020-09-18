export function modelValidator(model) {
  if (!model.children || model.children.length <= 0) {
    console.warn('tabs-wrapper needs tab-component children!', model.children)
    return false
  }

  const tabChildren = model.children.filter((child) => {
    return child.component === 'admin-components-tab'
  })

  if (tabChildren.length !== model.children.length) {
    console.warn('tabs-wrapper only allows tab-component children', model.children)
    return false
  }

  return true
}

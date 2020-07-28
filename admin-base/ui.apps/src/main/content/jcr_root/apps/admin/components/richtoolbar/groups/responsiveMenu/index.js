import {DropDown} from '../../../../../../../../js/constants'

export default (vm) => {
  const buttonSize = vm.size.button
  const groupSize = vm.size.group
  const items = []

  const breakpoint = () => {
    return 800
        - items.filter((i) => i !== DropDown.DIVIDER).length * buttonSize
        - (items.filter((i) => i === DropDown.DIVIDER).length + 1) * groupSize
  }
  const hideGroup = (group) => {
    vm.hiddenGroups[group.label] = true
    if (items.length >= 1) items.unshift(DropDown.DIVIDER)
    items.unshift(...group.items)
  }
  const showGroup = (group) => {
    vm.hiddenGroups[group.label] = false
    console.log(vm.hiddenGroups)
  }

  const w = vm.docEl.dimension.w
  const groups = vm.groups
  groups.pop()

  for (let i = groups.length - 1; i > 0; i--) {
    if (w <= breakpoint()) {
      hideGroup(groups[i])

      if (i === groups.length - 1) {
        i--
        hideGroup(groups[i])
      }
    } else {
      showGroup(groups[i])

      if (i === groups.length - 1) {
        i--
        showGroup(groups[i])
      }
    }
  }

  /*
  ...actionsGroup(vm).items,
        DropDown.DIVIDER,
        ...textFormatGroup(vm).items,
        DropDown.DIVIDER,
        ...boldItalicGroup(vm).items,
        DropDown.DIVIDER,
        ...superSubScriptGroup(vm).items,
        DropDown.DIVIDER,
        ...linkGroup(vm).items,
        DropDown.DIVIDER,
        ...imageGroup(vm).items,
        DropDown.DIVIDER,
   */
  return {
    label: 'responsive-menu',
    icon: 'bars',
    collapse: true,
    isActive: () => false,
    rules: () => vm.docEl.dimension.w <= 800,
    items
  }
}
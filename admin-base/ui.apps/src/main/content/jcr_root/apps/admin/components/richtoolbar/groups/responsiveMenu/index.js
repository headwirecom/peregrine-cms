import {DropDown} from '../../../../../../../../js/constants'

export default (vm) => {
  const buttonSize = vm.size.button
  const groupSize = vm.size.group
  let items = []

  const breakpoint = () => {
    return 800
        - items.filter((i) => i !== DropDown.DIVIDER).length * buttonSize
        - (items.filter((i) => i === DropDown.DIVIDER).length + 1) * groupSize
  }
  const hideGroup = (group) => {
    vm.$set(vm.hiddenGroups, group.label, true)
    if (items.length >= 1) {
      items = [DropDown.DIVIDER, ...items]
    }
    items = [...group.items, ...items]
  }

  const showGroup = (group) => {
    vm.$set(vm.hiddenGroups, group.label, false)
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

  return {
    label: 'responsive-menu',
    icon: 'bars',
    collapse: true,
    isActive: () => false,
    rules: () => vm.docEl.dimension.w <= 800,
    items
  }
}
import {DropDown} from '../../../../../../../../js/constants';

const baseBreakpoint = 1000

export default (vm) => {
  const w = vm.docEl.dimension.w
  const groups = vm.groups

  groups.forEach((g) => {
    if (w <= breakpoint(vm, g)) {
      hideGroup(vm, g)
    } else {
      showGroup(vm, g)
    }
  })

  return {
    label: 'responsive-menu',
    icon: 'bars',
    collapse: true,
    isActive: () => false,
    rules: () => vm.responsive && vm.docEl.dimension.w <= baseBreakpoint,
    items: getItems(vm)
  }
}

const getGroupIndex = (vm, group) => {
  let index = -1

  vm.groups.some((g, i) => {
    if (g.label === group.label && g.icon === group.icon) {
      index = i
      return true
    }
  })

  return index
}

const breakpoint = (vm, group) => {
  const size = vm.size
  const sliced = vm.groups.slice(0, getGroupIndex(vm, group))
  let br = lowestBreakpoint(vm)

  sliced.forEach((g) => {
    br += size.group
    if (!g.collapse) {
      br += g.items.length * size.button
    } else {
      br += size.button
    }
  })

  return br
}

const lowestBreakpoint = (vm) => {
  let lowest = baseBreakpoint
  const size = vm.size

  vm.groups.forEach((g) => {
    lowest -= size.group
    if (!g.collapse) {
      lowest -= g.items.length * size.button
    } else {
      lowest -= size.button
    }
  })

  return lowest
}

const hideGroup = (vm, group) => {
  vm.$set(vm.hiddenGroups, group.label, true)
}

const showGroup = (vm, group) => {
  vm.$set(vm.hiddenGroups, group.label, false)
}

const getItems = (vm) => {
  const all = vm.groups
  const filtered = vm.filteredGroups
  const items = []

  all.forEach((g) => {
    const c = filtered.filter((f) => f.label === g.label && f.icon === g.icon)

    if (!c || c.length <= 0) {
      if (items.length > 0) {
        items.push(DropDown.DIVIDER)
      }
      items.push(...g.items)
    }
  })

  return items
}
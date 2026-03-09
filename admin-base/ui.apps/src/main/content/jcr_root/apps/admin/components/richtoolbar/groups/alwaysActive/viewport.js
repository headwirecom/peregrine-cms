export default (vm) => {
  const items = [
    {
      id: 'mobile',
      label: vm.$i18n('mobile'),
      icon: 'phone_android',
      iconLib: 'material-icons',
      class: () => vm.viewport === 'mobile' ? 'active' : null,
      click: () => vm.setViewport('mobile')
    },
    {
      id: 'mobile-landscape',
      label: vm.$i18n('mobile-landscape'),
      icon: 'stay_current_landscape',
      iconLib: 'material-icons',
      class: () => vm.viewport === 'mobile-landscape' ? 'active' : null,
      click: () => vm.setViewport('mobile-landscape')
    },
    {
      id: 'tablet',
      label: vm.$i18n('tablet'),
      icon: 'tablet_android',
      iconLib: 'material-icons',
      class: () => vm.viewport === 'tablet' ? 'active' : null,
      click: () => vm.setViewport('tablet')
    },
    {
      id: 'tablet-landscape',
      label: vm.$i18n('tablet-landscape'),
      icon: 'tablet',
      iconLib: 'material-icons',
      class: () => vm.viewport === 'tablet-landscape' ? 'active' : null,
      click: () => vm.setViewport('tablet-landscape')
    },
    {
      id: 'laptop',
      label: vm.$i18n('laptop'),
      icon: 'laptop_windows',
      iconLib: 'material-icons',
      class: () => vm.viewport === 'laptop' ? 'active' : null,
      click: () => vm.setViewport('laptop')
    },
    {
      id: 'desktop',
      label: vm.$i18n('desktop'),
      icon: 'desktop_windows',
      iconLib: 'material-icons',
      class: () => !vm.viewport || vm.viewport === 'desktop' ? 'active'
          : null,
      click: () => vm.setViewport('desktop')
    }
  ]

  const getCurrentItem = () => items.find(item => item.id === vm.viewport) || items[items.length - 1]

  return {
    label: () => getCurrentItem().label,
    iconLib: 'material-icons',
    class: 'always-active separate',
    collapse: true,
    isActive: () => false,
    icon: () => getCurrentItem().icon,
    items
  }
}

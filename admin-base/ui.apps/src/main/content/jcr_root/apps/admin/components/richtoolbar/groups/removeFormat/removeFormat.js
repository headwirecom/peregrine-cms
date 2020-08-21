import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'remove format',
    icon: 'format_clear',
    iconLib: IconLib.MATERIAL_ICONS,
    rules: () => !vm.responsive || !vm.hiddenGroups['remove format'],
    cmd: 'removeFormat'
  }
}
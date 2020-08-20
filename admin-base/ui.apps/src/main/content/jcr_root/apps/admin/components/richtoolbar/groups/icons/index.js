import icons from './icons'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'bold-italic',
    icon: 'bold',
    iconLib: IconLib.FONT_AWESOME,
    collapse: true,
    rules: () => !vm.responsive || !vm.hiddenGroups['bold-italic'],
    items: [
      ...icons(vm)
    ]
  }
}
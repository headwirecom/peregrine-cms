import bold from './bold'
import italic from './italic'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'bold-italic',
    icon: 'bold',
    iconLib: IconLib.FONT_AWESOME,
    rules: () => !vm.responsive || !vm.hiddenGroups['bold-italic'],
    items: [
      bold(vm),
      italic(vm)
    ]
  }
}
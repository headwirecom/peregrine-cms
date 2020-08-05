import numberedList from './numberedList'
import bulletedList from './bulletedList'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'list',
    icon: 'list',
    iconLib: IconLib.FONT_AWESOME,
    rules: () => !vm.responsive || !vm.hiddenGroups.list,
    items: [
      numberedList(vm),
      bulletedList(vm)
    ]
  }
}
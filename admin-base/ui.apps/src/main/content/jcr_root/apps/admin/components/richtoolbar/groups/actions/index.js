import redo from './redo'
import undo from './undo'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'actions',
    icon: 'undo',
    iconLib: IconLib.FONT_AWESOME,
    rules: () => !vm.responsive || !vm.hiddenGroups.actions,
    items: [
      undo(vm),
      redo(vm)
    ]
  }
}
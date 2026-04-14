import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'redo',
    icon: 'repeat',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'redo',
    isDisabled: () => vm.onSubNav ? vm.sharedHistoryIndex >= vm.sharedHistoryLength - 1 : vm.historyIndex >= vm.historyStack.length - 1
  }
}

import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'underline',
    icon: 'underline',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'underline',
    isActive: () => vm.queryCmdState('underline'),
    isDisabled: () => !vm.hasEditorSelection
  }
}

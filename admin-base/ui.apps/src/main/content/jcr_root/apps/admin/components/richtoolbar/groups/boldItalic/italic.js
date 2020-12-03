import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'italic',
    icon: 'italic',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'italic',
    isActive: () => vm.queryCmdState('italic')
  }
}

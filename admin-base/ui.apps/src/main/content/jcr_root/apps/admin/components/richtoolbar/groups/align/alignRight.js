import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'align right',
    icon: 'align-right',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'justifyRight',
    isActive: () => vm.queryCmdState('justifyRight')
  }
}

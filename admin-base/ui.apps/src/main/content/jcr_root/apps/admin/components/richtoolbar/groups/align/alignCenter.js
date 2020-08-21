import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'align center',
    icon: 'align-center',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'justifyCenter',
    isActive: () => vm.queryCmdState('justifyCenter')
  }
}

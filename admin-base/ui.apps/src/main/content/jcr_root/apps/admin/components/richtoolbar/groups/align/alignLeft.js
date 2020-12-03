import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'align left',
    icon: 'align-left',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'justifyLeft',
    isActive: () => vm.queryCmdState('justifyLeft')
  }
}

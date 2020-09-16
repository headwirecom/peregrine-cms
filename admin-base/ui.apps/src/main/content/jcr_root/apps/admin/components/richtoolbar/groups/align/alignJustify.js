import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'align justify',
    icon: 'align-justify',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'justifyFull',
    isActive: () => vm.queryCmdState('justifyFull')
  }
}

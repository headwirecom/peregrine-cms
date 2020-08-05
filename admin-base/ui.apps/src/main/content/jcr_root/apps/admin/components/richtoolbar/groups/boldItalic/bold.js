import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'bold',
    icon: 'bold',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'bold',
    isActive: () => vm.queryCmdState('bold')
  }
}

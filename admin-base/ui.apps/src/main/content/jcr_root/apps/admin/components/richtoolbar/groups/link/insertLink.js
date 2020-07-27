import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'insert link',
    icon: 'link',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'link',
    isActive: () => vm.itemIsTag('A')
  }
}
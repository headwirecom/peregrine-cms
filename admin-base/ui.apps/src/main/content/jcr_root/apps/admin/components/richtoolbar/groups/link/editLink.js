import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'edit link',
    icon: 'pencil',
    iconLib: IconLib.FONT_AWESOME,
    click: () => vm.editLink(),
    isActive: () => vm.itemIsTag('A')
  }
}
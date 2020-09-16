import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'remove link',
    icon: 'chain-broken',
    iconLib: IconLib.FONT_AWESOME,
    click: () => vm.removeLink(),
    isActive: () => vm.itemIsTag('A')
  }
}
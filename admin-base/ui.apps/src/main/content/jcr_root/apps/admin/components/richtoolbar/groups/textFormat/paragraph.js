import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: vm.$i18n('paragraph'),
    icon: 'paragraph',
    iconLib: IconLib.FONT_AWESOME,
    class: () => vm.itemIsTag('P') ? 'active' : null,
    click: () => vm.exec('formatBlock', 'p')
  }
}

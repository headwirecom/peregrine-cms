import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'bulleted list',
    icon: 'list-ul',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'insertUnorderedList',
    isActive: () => vm.itemIsTag('insertUnorderedList')
  }
}
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'numbered list',
    icon: 'list-ol',
    iconLib: IconLib.FONT_AWESOME,
    cmd: 'insertOrderedList',
    isActive: () => vm.itemIsTag('insertOrderedList')
  }
}
import headlines from './headlines'
import paragraph from './paragraph'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'text-format',
    icon: 'paragraph',
    iconLib: IconLib.FONT_AWESOME,
    collapse: true,
    rules: () => !vm.responsive || !vm.hiddenGroups['text-format'],
    items: [
      paragraph(vm),
      ...headlines(vm)
    ]
  }
}
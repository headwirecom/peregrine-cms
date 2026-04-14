import icons from './icons'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'icons',
    icon: 'flag',
    iconLib: IconLib.FONT_AWESOME,
    collapse: true,
    searchable: true,
    isDisabled: () => !vm.hasEditorSelection,
    rules: () => !vm.responsive || !vm.hiddenGroups['icons'],
    items: [
      ...icons(vm)
    ],
    toggleClick() {
      if (vm.getInlineDoc()) vm.saveSelection()
    }
  }
}

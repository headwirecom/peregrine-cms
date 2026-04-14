import specialCharacters from './specialCharacters'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'special-characters',
    icon: 'copyright',
    iconLib: IconLib.FONT_AWESOME,
    collapse: true,
    searchable: true,
    isDisabled: () => !vm.hasEditorSelection && !vm._specialCharRange,
    rules: () => !vm.responsive || !vm.hiddenGroups['special-characters'],
    items: [
      ...specialCharacters(vm)
    ],
    toggleClick() {
      vm.saveSelection()
      const doc = vm.getInlineDoc()
      if (doc && doc.defaultView) {
        const sel = doc.defaultView.getSelection()
        vm._specialCharRange = (sel && sel.rangeCount > 0)
          ? sel.getRangeAt(0).cloneRange()
          : null
      } else {
        vm._specialCharRange = null
      }
    }
  }
}

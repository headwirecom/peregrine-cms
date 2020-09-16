import specialCharacters from './specialCharacters'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'special-characters',
    icon: 'copyright',
    iconLib: IconLib.FONT_AWESOME,
    collapse: true,
    searchable: true,
    rules: () => !vm.responsive || !vm.hiddenGroups['special-characters'],
    items: [
      ...specialCharacters(vm)
    ],
    toggleClick() {
      vm.saveSelection()
    }
  }
}
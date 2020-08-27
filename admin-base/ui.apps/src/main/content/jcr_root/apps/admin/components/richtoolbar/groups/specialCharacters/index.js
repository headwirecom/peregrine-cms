import icons from './specialCharacters'
import {IconLib} from '../../../../../../../../js/constants'
import HtmlEncoder from '../../util/HtmlEncoder'

export default (vm) => {
  console.log('specialCharacter.specialCharacters:', HtmlEncoder.arr1)

  return {
    label: 'special-characters',
    icon: 'emoji_symbols',
    iconLib: IconLib.MATERIAL_ICONS,
    collapse: true,
    rules: () => !vm.responsive || !vm.hiddenGroups['special-characters'],
    items: [
      ...icons(vm)
    ]
  }
}
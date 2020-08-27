import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return [
    {
      label: '&',
      icon: '&',
      iconLib: IconLib.PLAIN_TEXT,
      click: () => {
        vm.insertSpecialCharacter('&')
      }
    },
    {
      label: '€',
      icon: '€',
      iconLib: IconLib.PLAIN_TEXT,
      click: () => {
        vm.insertSpecialCharacter('€')
      }
    }
  ]
}
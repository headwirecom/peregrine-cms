import {simpleCharMap} from '../../../../../../../../js/utils/charMap'
import HtmlEncoder from '../../../../../../../../js/utils/htmlEncoder'

export default (vm) => {
  const specialChars = []

  simpleCharMap.forEach((char) => {
    specialChars.push({
      label: HtmlEncoder.htmlDecode(`&#${char.code};`),
      title: char.name,
      name: char.name,
      click: () => {
        vm.restoreSelection()
        vm.$nextTick(() => {
          vm.execCmd('insertHTML', `&#${char.code};`)
        })
      }
    },)
  })

  return specialChars
}
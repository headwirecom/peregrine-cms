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
        const container = vm.selection.container
        const doc = vm.selection.doc
        if (container && doc) {
          container.focus({ preventScroll: true })
          const range = vm._specialCharRange
          if (range) {
            const sel = doc.defaultView.getSelection()
            if (sel) {
              sel.removeAllRanges()
              sel.addRange(range.cloneRange())
            }
            vm._specialCharRange = null
          }
        } else {
          vm.restoreSelection()
        }
        vm.execCmd('insertHTML', `&#${char.code};`)
      }
    },)
  })

  return specialChars
}

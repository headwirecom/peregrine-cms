import htmlEncoder from '../../../../../../../../js/utils/htmlEncoder'

export default (vm) => {
  const specialChars = []

  htmlEncoder.arr2.forEach((encoded) => {
    const decoded = htmlEncoder.htmlDecode(encoded)

    specialChars.push({
      label: decoded,
      click: () => {
        vm.execCmd('insertHTML', `${encoded}`)
      }
    },)
  })

  return specialChars
}
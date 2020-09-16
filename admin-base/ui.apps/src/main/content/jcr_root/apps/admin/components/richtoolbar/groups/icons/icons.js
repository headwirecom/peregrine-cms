import {IconLib} from '../../../../../../../../js/constants'
import {LoggerFactory} from '../../../../../../../../js/logger'
import {get} from '../../../../../../../../js/utils'

const FILE_NAME = 'richtoolbar.groups.icons.icon'
const log = LoggerFactory.logger(FILE_NAME).setLevelDebug()

function getChildName(child) {
  let arr = child.name.split('.')

  arr.pop()

  return arr.join('.')
}

function getChildImage(child) {
  return `<img src="${child.path}" alt="${child.name}"/>`
}

export default (vm) => {
  const tenantIcons = get($perAdminApp.getView(), '/admin/icons', [])
  const icons = []

  tenantIcons.forEach((child) => {
    const name = getChildName(child)
    const img = getChildImage(child)

    icons.push({
      label: name,
      icon: img,
      iconLib: IconLib.PLAIN_TEXT,
      click: () => {
        vm.insertIcon(name)
      },
    })
  })

  return icons
}
import {IconLib} from '../../../../../../../../js/constants'
import {LoggerFactory} from '../../../../../../../../js/logger';

const FILE_NAME = 'richtoolbar.groups.icons.icon'
const log = LoggerFactory.logger(FILE_NAME).setLevelDebug()
const children = []

function populateIcons(vm) {
  if (children.length > 0) return

  const tenant = $perAdminApp.getView().state.tenant

  return $perAdminApp
      .getApi()
      .getIcons(tenant)
      .then((data) => {
        children.push(...data.children)
        vm.pingRichToolbar()
      })
      .catch((err) => {
        log.error(`failed loading icons node for ${FILE_NAME}`, err)
      })
}

function getChildName(child) {
  let arr = child.name.split('.')

  arr.pop()

  return arr.join('.')
}

function getChildImage(child) {
  return `<img src="${child.path}" alt="${child.name}"/>`
}

export default (vm) => {
  populateIcons(vm)

  const icons = []

  children.forEach((child) => {
    const name = getChildName(child)
    const img = getChildImage(child)

    icons.push({
      label: name,
      icon: img,
      iconLib: IconLib.PLAIN_TEXT,
      click: () => {
        vm.param.cmd = 'insertImage'
        vm.browser.path.selected = child.path
        vm.browser.linkTitle = child.name
        vm.onBrowserSelect()
      },
    })
  })

  return icons
}
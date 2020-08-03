import insertLink from './insertLink'
import editLink from './editLink'
import removeLink from './removeLink'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  const link = {
    label: 'link',
    icon: 'link',
    iconLib: IconLib.FONT_AWESOME,
    rules: () => !vm.responsive || !vm.hiddenGroups.link,
        items: [
        insertLink(vm)
    ]
  }

  if (vm.itemIsTag('A')) {
    link.collapse = true
    link.items = [
        editLink(vm),
        removeLink(vm)
    ]
  }

  return link
}
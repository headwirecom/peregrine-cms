import insertLink from './insertLink'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  const link = {
    _key: vm.key,
    label: 'link',
    icon: 'link',
    iconLib: IconLib.FONT_AWESOME,
    rules: () => !vm.responsive || !vm.hiddenGroups.link,
    items: [
      insertLink(vm)
    ]
  }

  return link
}

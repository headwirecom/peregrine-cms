import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'superscript',
    icon: 'A<sup>2</sup>',
    iconLib: IconLib.PLAIN_TEXT,
    cmd: 'superscript',
    isActive: () => vm.itemIsTag('SUP')
  }
}

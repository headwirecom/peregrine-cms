import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'subscript',
    icon: 'A<sub>2</sub>',
    iconLib: IconLib.PLAIN_TEXT,
    cmd: 'subscript',
    isActive: () => vm.itemIsTag('SUB')
  }
}

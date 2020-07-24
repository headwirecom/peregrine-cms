import superscript from './superscript'
import subscript from './subscript'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'bold-italic',
    icon: 'bold',
    iconLib: IconLib.FONT_AWESOME,
    items: [
      superscript(vm),
      subscript(vm)
    ]
  }
}
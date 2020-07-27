import insertImage from './insertImage'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'insert image',
    icon: 'picture-o',
    iconLib: IconLib.FONT_AWESOME,
    items: [
      insertImage(vm)
    ]
  }
}
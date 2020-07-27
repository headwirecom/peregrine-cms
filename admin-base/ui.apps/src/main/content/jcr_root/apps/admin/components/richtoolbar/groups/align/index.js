import alignLeft from './alignLeft'
import alignCenter from './alignCenter'
import alignRight from './alignRight'
import alignJustify from './alignJustify'
import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'justify',
    icon: 'align-justify',
    iconLib: IconLib.FONT_AWESOME,
    items: [
        alignLeft(vm),
        alignCenter(vm),
        alignRight(vm),
        alignJustify(vm)
    ]
  }
}
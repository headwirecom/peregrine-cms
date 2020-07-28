import {
  actionsGroup,
  alignGroup,
  boldItalicGroup,
  imageGroup,
  linkGroup,
  listGroup,
  removeFormatGroup,
  superSubScriptGroup,
  textFormatGroup
} from '../'
import {DropDown} from '../../../../../../../../js/constants';

export default (vm) => {

  return {
    __enforceUpdate__: vm.docEl.dimension.w,
    label: 'always-active',
    icon: 'bars',
    collapse: true,
    isActive: () => false,
    items: [
      ...actionsGroup(vm).items,
      DropDown.DIVIDER,
      ...alignGroup(vm).items,
      DropDown.DIVIDER,
      ...boldItalicGroup(vm).items,
      DropDown.DIVIDER,
      ...imageGroup(vm).items,
      DropDown.DIVIDER,
      ...linkGroup(vm).items,
      DropDown.DIVIDER,
      ...listGroup(vm).items,
      DropDown.DIVIDER,
      ...removeFormatGroup(vm).items,
      DropDown.DIVIDER,
      ...superSubScriptGroup(vm).items,
      DropDown.DIVIDER,
      ...textFormatGroup(vm).items
    ]
  }
}
import {
  actionsGroup,
  alignGroup,
  allMenuGroup,
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
      actionsGroup(vm),
      DropDown.DIVIDER,
      textFormatGroup(vm),
      DropDown.DIVIDER,
      boldItalicGroup(vm),
      DropDown.DIVIDER,
      superSubScriptGroup(vm),
      DropDown.DIVIDER,
      linkGroup(vm),
      DropDown.DIVIDER,
      imageGroup(vm),
      DropDown.DIVIDER,
      alignGroup(vm),
      DropDown.DIVIDER,
      listGroup(vm),
      DropDown.DIVIDER,
      removeFormatGroup(vm),
      DropDown.DIVIDER,
      allMenuGroup(vm)
    ]
  }
}
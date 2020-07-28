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
      actionsGroup(this),
      DropDown.DIVIDER,
      textFormatGroup(this),
      DropDown.DIVIDER,
      boldItalicGroup(this),
      DropDown.DIVIDER,
      superSubScriptGroup(this),
      DropDown.DIVIDER,
      linkGroup(this),
      DropDown.DIVIDER,
      imageGroup(this),
      DropDown.DIVIDER,
      alignGroup(this),
      DropDown.DIVIDER,
      listGroup(this),
      DropDown.DIVIDER,
      removeFormatGroup(this),
      DropDown.DIVIDER,
      allMenuGroup(this)
    ]
  }
}
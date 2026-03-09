import {IconLib} from '../../../../../../../../js/constants'

export default (vm) => {
  return {
    label: 'remove format',
    icon: 'format_clear',
    iconLib: IconLib.MATERIAL_ICONS,
    rules: () => !vm.responsive || !vm.hiddenGroups['remove format'],
    cmd: 'removeFormat',
    isDisabled: () => {
      if (!vm.hasEditorSelection) return true
      const range = vm.getEditorSelection()
      if (!range) return true
      const container = range.startContainer
      const el = container.nodeType === Node.TEXT_NODE ? container.parentElement : container
      // Check if cursor is inside a formatting tag
      if (el && el.closest('b,strong,i,em,u,s,del,strike,font')) return false
      // Check queryCommandState via the range's own document
      const doc = container.ownerDocument
      if (['bold', 'italic', 'underline', 'strikeThrough'].some(cmd => {
        try { return doc.queryCommandState(cmd) } catch (e) { return false }
      })) return false
      if (range.collapsed) return true
      // Range selection: inspect cloned content for formatting tags or formatting inline styles
      const tmp = document.createElement('div')
      tmp.appendChild(range.cloneContents())
      if (tmp.querySelector('b,strong,i,em,u,s,del,strike,font')) return false
      const hasFmtStyle = (e) => !!(e.style && (
        e.style.fontSize || e.style.color || e.style.backgroundColor ||
        e.style.fontFamily || e.style.fontWeight || e.style.fontStyle || e.style.textDecoration
      ))
      return ![...tmp.querySelectorAll('[style]')].some(hasFmtStyle)
    }
  }
}

/**
 * Shared richtext command utilities.
 */

export function getExecDoc() {
  const view = $perAdminApp.getView()
  if (!view) return document
  const inline = view.state && view.state.inline
  return (inline && (inline.doc || inline.lastDoc)) || document
}

export function isIframeContext(execDoc) {
  return !!(execDoc && execDoc !== document)
}

export function writeToModel(vm, execDoc) {
  $perAdminApp.action(vm, 'pingRichToolbar')
  if (isIframeContext(execDoc)) {
    $perAdminApp.action(vm, 'writeInlineToModel')
  } else {
    $perAdminApp.action(vm, 'textEditorWriteToModel')
  }
}

export function execRichCommand(vm, cmd) {
  const doc = getExecDoc()
  if (doc && doc.execCommand) doc.execCommand(cmd)
  writeToModel(vm, doc)
}

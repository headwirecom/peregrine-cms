import preview from './preview';
import viewport from './viewport';

export default (vm) => {
  return {
    label: 'always-active',
    noCollapse: true,
    rules: () => vm.showAlwaysActive,
    items: [
        preview(vm),
        viewport(vm)
    ]
  }
}
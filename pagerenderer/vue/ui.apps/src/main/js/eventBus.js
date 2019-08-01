const EventBus = {
  install(v, options){
    v.prototype.$eventBus = window.parent.$perAdminApp.getApp().$adminEventBus;
  }
};
export default EventBus;

const EventBus = {
  install(v, options){
    if( window.parent.$perAdminApp ){
      v.prototype.$eventBus = window.parent.$perAdminApp.getApp().$adminEventBus;
    } else {
      v.prototype.$eventBus = new Vue();
    }
  }
};
export default EventBus;

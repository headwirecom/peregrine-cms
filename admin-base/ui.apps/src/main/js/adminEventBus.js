const AdminEventBus = {
  install(v, options){
    v.prototype.$adminEventBus = new Vue();
    v.prototype.$adminEventBus.healthCheck = () => { return 200;}
  }
};
export default AdminEventBus;

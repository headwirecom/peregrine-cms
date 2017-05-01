import PerAdminApp from './perAdminApp'
import PerAdminImpl from './apiImpl'

var $pappView = {}
var $papp = new PerAdminApp($pappView)
var $perApi = new PerAdminImpl($papp)
$papp.setApi($perApi)
var $logger = $papp.getLogger('index.js')
$papp.getApi().populateTools().then(() => { $logger.info(JSON.stringify($pappView, true, 2)) } ).catch( (error) => $logger.info('failed test') )


export default {
    data() {
        return {

        }
    },
    methods: {
        trimReferences(referenceList) {
            return referenceList.reduce(
              (map => (r, a) => (!map.has(a.path) && map.set(a.path, 
              r[r.push({
                name: a.name,
                path: a.path,
                propertyName: a.propertyName,
                propertyPath: a.propertyPath,
                count: 0,
                activated: a.activated,
                is_stale: a.is_stale,
                "jcr:lastModified": a["jcr:lastModified"],
                "per:Replicated": a["per:Replicated"],
              }) - 1]), 
              map.get(a.path).count++, r))(new Map),
              []
            );
          },
          printStatus(ref){
            if (ref.is_stale) {
                return "Stale"
            } else if (ref.activated === true){
                return "Published"                
            } else if(ref.activated === false) {
                return "Unpublished"
            } else {
                return "No Status"
            }
        },
    }
}
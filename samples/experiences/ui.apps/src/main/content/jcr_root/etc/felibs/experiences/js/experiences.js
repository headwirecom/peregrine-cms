if(window.$peregrineApp) {
    var $experiences = []

    $peregrineApp.setExperience = function(experience) {
        $experiences.push(experience)
    }

    $peregrineApp.getExperiences = function() {
        return $experiences
    }

    if(document.location.hash) {
        const hash = document.location.hash.slice(1)
        $peregrineApp.setExperience(hash)
    }
}


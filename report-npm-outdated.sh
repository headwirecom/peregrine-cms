
PKG_ORDER=( \
  ./ \
  ./admin-base/materialize \
  ./admin-base/ui.apps \
  ./themes/themeclean/ui.apps \
  ./buildscripts \
  ./samples/example-vue-site/ui.apps \
  ./pagerenderer/vue/ui.apps \
)

for pkg in "${PKG_ORDER[@]}"
do
  pushd $pkg > /dev/null
  echo === $(pwd)
  npm outdated
  echo =
  popd > /dev/null
done

for pkg in "${PKG_ORDER[@]}"
do
  pushd $pkg > /dev/null
  echo === $(pwd)
  npm audit
  echo =
  popd > /dev/null
done

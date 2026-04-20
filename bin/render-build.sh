#!/usr/bin/env bash
# bin/render-build.sh
set -o errexit   # exit immediately if any command fails

bundle install
bundle exec rake assets:precompile
bundle exec rake assets:clean
bundle exec rails db:migrate
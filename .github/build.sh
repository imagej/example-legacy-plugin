#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/main/ci-build.sh
# Note: We skip deploy because this example is not intended for use as a dependency.
BUILD_ARGS=-Dmaven.deploy.skip sh ci-build.sh

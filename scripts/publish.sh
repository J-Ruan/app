#!/usr/bin/env bash
# Run only after restoring GitHub access. Publishes the committed main branch and a release tag.
set -euo pipefail
cd "$(dirname "$0")/.."
version="${1:-v1.0.0}"
[[ "$version" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo 'Use a version such as v1.0.0' >&2; exit 1; }
[[ -z "$(git status --porcelain)" ]] || { echo 'Commit or preserve your changes before publishing.' >&2; exit 1; }
git ls-remote origin HEAD >/dev/null
./gradlew assembleDebug testDebugUnitTest lintDebug
if git rev-parse --verify "refs/tags/$version" >/dev/null 2>&1; then
  [[ "$(git rev-parse "$version^{commit}")" == "$(git rev-parse HEAD)" ]] || { echo 'Version tag points to another commit.' >&2; exit 1; }
else
  git tag "$version"
fi
git push --atomic origin HEAD:refs/heads/main "refs/tags/$version"
echo 'Source and tag pushed. GitHub Actions will build the downloadable Debug APK and publish the Release.'

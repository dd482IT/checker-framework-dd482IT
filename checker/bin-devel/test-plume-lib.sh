#!/bin/bash

set -e
set -o verbose
set -o xtrace
export SHELLOPTS
echo "SHELLOPTS=${SHELLOPTS}"

# Optional argument $1 is the group.
GROUPARG=$1
echo "GROUPARG=$GROUPARG"
# These are all the Java projects at https://github.com/plume-lib as of Dec 2022.
if [[ "${GROUPARG}" == "bcel-util" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "bibtex-clean" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "html-pretty-print" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "icalavailable" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "javadoc-lookup" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "lookup" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "multi-version-control" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "options" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "plume-util" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "reflection-util" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "require-javadoc" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "all" ]] || [[ "${GROUPARG}" == "" ]]; then
  PACKAGES=(bcel-util bibtex-clean html-pretty-print icalavailable javadoc-lookup lookup multi-version-control options plume-util reflection-util require-javadoc)
fi
if [ -z ${PACKAGES+x} ]; then
  echo "Bad group argument '${GROUPARG}'"
  exit 1
fi
echo "PACKAGES=" "${PACKAGES[@]}"


SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# shellcheck disable=SC1090 # In newer shellcheck than 0.6.0, pass: "-P SCRIPTDIR" (literally)
source "$SCRIPTDIR"/build.sh


echo "PACKAGES=" "${PACKAGES[@]}"
for PACKAGE in "${PACKAGES[@]}"; do
  echo "PACKAGE=${PACKAGE}"
  PACKAGEDIR="/tmp/${PACKAGE}"
  rm -rf "${PACKAGEDIR}"
  "$SCRIPTDIR/.plume-scripts/git-clone-related" plume-lib "${PACKAGE}" "${PACKAGEDIR}"
  # Uses "compileJava" target instead of "assemble" to avoid the javadoc error "Error fetching URL:
  # https://docs.oracle.com/en/java/javase/17/docs/api/" due to network problems.
  echo "About to call ./gradlew --console=plain -PcfLocal compileJava"
  # Try twice in case of network lossage.
  (cd "${PACKAGEDIR}" && (./gradlew --console=plain -PcfLocal compileJava || (sleep 60 && ./gradlew --console=plain -PcfLocal compileJava)))
done

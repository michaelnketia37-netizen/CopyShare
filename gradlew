#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a symlink
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -ld "$PRG"
    PRG=`readlink "$PRG"`
done
SAVED="$(cd -P "$(dirname "$PRG")" >/dev/null 2>&1 && pwd)"
cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
APP_HOME="$(cd -P .)" >/dev/null 2>&1 || exit 1
cd "$SAVED" >/dev/null 2>&1

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='" -Xmx64m" -Xms64m'

# Use the maximum available, or set MAX_FD != unlimited.
MAX_FD="maximum"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
IS_CYGWIN=false
IS_MINGW=false
IS_MSYS=false
IS_WINDOWS=false
case "$(uname)" in
  CYGWIN* )
    IS_CYGWIN=true
    IS_WINDOWS=true
    ;;
  MINGW* )
    IS_MINGW=true
    IS_WINDOWS=true
    ;;
  MSYS* )
    IS_MSYS=true
    IS_WINDOWS=true
    ;;
  NATU* )
    IS_WINDOWS=true
    ;;
esac

# Determine the Java command to use in order to create the initial classpath
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! "$cygpath_cmd" /w / >/dev/null 2>&1 ; then
    die "cygpath $_cmd does not work"
fi

# For the initial version, let's limit to the default.
if [ "$IS_WINDOWS" = "true" ] ;
then
    MAX_FD="$( expr $MAX_FD )"
fi

if [ "$MAX_FD" = "maximum" ] ; then
    if [ "$IS_WINDOWS" = "true" ] ; then
        MAX_FD=32768
    else
        MAX_FD=`ulimit -H -n`
        if [ $? -eq 0 ] ; then
            if [ "$MAX_FD" = "unlimited" ] ; then
                MAX_FD=32768
            fi
        fi
    fi
fi

if [ "$MAX_FD" != "unlimited" ] ; then
    ulimit -n $MAX_FD
    if [ $? -ne 0 ] ; then
        warn "Could not set maximum file descriptor limit: $MAX_FD"
    fi
else
    warn "Could not query maximum file descriptor limit: $MAX_FD"
fi

# For Darwin, add -Xms to handle the first access
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS -Xms64m"
fi

# Collect all arguments for the java command, stacking in reverse order
args=()
for arg in "$@" ; do
    case $arg in
        -\?|-h|--help )
            showMessage Instructions
            exit 0
            ;;
        -v|--version )
            showMessage GradleVersion
            exit 0
            ;;
        * )
            args+=("$arg")
            ;;
    esac
done

set -- "${args[@]}"

DART_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -r "$DART_WRAPPER_JAR" ] ; then
    die "ERROR: Unable to locate gradle-wrapper.jar at location: $DART_WRAPPER_JAR"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -classpath "$DART_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"

#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p /workspace/downloads /workspace/toolchains /workspace/android-user /workspace/gradle-home
# Install a full JDK, retaining upstream checksum and TLS verification.
if [[ ! -x /workspace/toolchains/jdk-21.0.5+11/bin/javac ]]; then
  jdk_url='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_linux_hotspot_21.0.5_11.tar.gz'
  curl -fsSL "$jdk_url" -o /workspace/downloads/jdk.tar.gz
  curl -fsSL "$jdk_url.sha256.txt" -o /workspace/downloads/jdk.sha256
  python3 - <<'PY'
import pathlib,hashlib
p=pathlib.Path('/workspace/downloads')
assert hashlib.sha256((p/'jdk.tar.gz').read_bytes()).hexdigest()==(p/'jdk.sha256').read_text().split()[0], 'JDK checksum mismatch'
PY
  tar -xzf /workspace/downloads/jdk.tar.gz -C /workspace/toolchains
fi
source scripts/cloud-env.sh
# Standard SDK archives from Google's repository metadata, with published checksums.
python3 scripts/install-sdk.py
# Respect the environment proxy and system CA store. Never disable TLS verification.
python3 - <<'PY'
import os,pathlib,urllib.parse
p=pathlib.Path(os.environ['GRADLE_USER_HOME'])/'gradle.properties'
s=p.read_text() if p.exists() else ''
for protocol in ['http','https']:
 u=urllib.parse.urlsplit(os.environ.get(protocol.upper()+'_PROXY',''))
 if u.hostname and not u.username and 'systemProp.'+protocol+'.proxyHost=' not in s:
  s+='\nsystemProp.'+protocol+'.proxyHost='+u.hostname+'\nsystemProp.'+protocol+'.proxyPort='+str(u.port or 80)+'\n'
if pathlib.Path('/etc/ssl/certs/java/cacerts').exists() and 'systemProp.javax.net.ssl.trustStore=' not in s:
 s+='\nsystemProp.javax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts\n'
p.write_text(s)
PY
./gradlew --no-daemon assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest

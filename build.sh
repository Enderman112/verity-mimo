#!/usr/bin/env bash
# Build Verity MiMo Direct -> a single standalone jar
set -euo pipefail
cd "$(dirname "$0")"

STUBS_SRC=stubs
SRC=src/main/java
RES=src/main/resources
CP_LIBS="build/lib/gson.jar:build/lib/yacl.jar:build/lib/mixin.jar:build/lib/annotations.jar:build/lib/guava.jar"

rm -rf build/stubs-classes build/classes build/out
mkdir -p build/stubs-classes build/classes build/out

echo "== compiling stubs =="
javac --release 17 -d build/stubs-classes $(find "$STUBS_SRC" -name '*.java')

echo "== compiling sources =="
javac --release 17 -encoding UTF-8 -cp "build/stubs-classes:$CP_LIBS" -d build/classes \
  $(find "$SRC" -name '*.java')

echo "== packaging jar =="
rm -f Verity-MiMo-Direct.jar   # remove stale jar first: zip -r would otherwise keep deleted classes
mkdir -p build/out/META-INF
printf 'Manifest-Version: 1.0\nMixinConfigs: verity_mimo_direct.mixins.json\n\n' > build/out/META-INF/MANIFEST.MF
cp -r "$RES"/* build/out/
cp -r build/classes/* build/out/
(cd build/out && zip -q -r -9 ../../Verity-MiMo-Direct.jar .)

echo "== done: $(pwd)/Verity-MiMo-Direct.jar =="

JAVA_VER := $(shell java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1 | sed 's/-ea//')

.PHONY: all

all: clean
	- ../../../gradlew build > Out.txt 2>&1
	grep -qF "Demo.java:5: warning: [CollectionIncompatibleType] Argument 'i - 1' should not be passed to this method; its type int is not compatible with its collection's type argument Short" Out.txt
	grep -qF "Demo.java:6: error: [argument] incompatible argument for parameter arg0 of add." Out.txt

clean:
	../../../gradlew clean
	rm -f Out.txt

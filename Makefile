
COMPONENT=framework
FULL_COMPONENT=hsn2-${COMPONENT}

all:	${FULL_COMPONENT}-package

clean:	${FULL_COMPONENT}-package-clean

hsn2-framework-package:
	mvn clean install -U -Pbundle -Dmaven.test.skip
	mkdir -p build/${COMPONENT}
	mkdir -p build/etc/hsn2/
	tar xzf hsn2-main/target/hsn2-main-1.0.0-SNAPSHOT.tar.gz -C build/${COMPONENT}
	cp hsn2-configuration/src/main/resources/defaultConfig.cfg build/etc/hsn2/framework.conf

hsn2-framework-package-clean:
	rm -rf build

build-local:
	mvn clean install -U -Pbundle
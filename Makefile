
all:	hsn2-framework-package

clean:	hsn2-framework-package-clean

hsn2-framework-package: hsn2-framework-package-clean
	mvn clean install -U -Pbundle
	mkdir -p build/framework
	mkdir -p build/etc/hsn2/
	tar xzf hsn2-main/target/hsn2-main-1.0.0-SNAPSHOT.tar.gz -C build/framework
	cp hsn2-configuration/src/main/resources/defaultConfig.cfg build/etc/hsn2/framework.conf

hsn2-framework-package-clean:
	rm -rf build

build-local:
	mvn clean install -U -Pbundle
# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1

SOURCEPATH=src
COMPILER=javac -sourcepath $(SOURCEPATH) -classpath build -d build
BUILDPATH=build
UNPACKTESTPATH=test-unpack

all: 
	$(COMPILER) @source_files 
	cp src/*.* build
	jar cfm RecipeJar.jar manifest -C build .

mainframe: src/recipejar/MainFrame.java
	$(COMPILER) src/recipejar/MainFrame.java
	cp src/*.* build
	jar cfm RecipeJar.jar manifest -C build .

prefs: src/recipejar/PreferencesDialog.java
	$(COMPILER) src/recipejar/PreferencesDialog.java
	cp src/*.* build
	jar cfm RecipeJar.jar manifest -C build .

.PHONY: clean
clean:
ifneq ("$(wildcard $(BUILDPATH))","")
	@echo test
	rm -r build
endif
ifneq ("$(wildcard $(UNPACKTESTPATH))","")
	@echo test
	rm -r test-unpack
endif

.PHONY: test
test:
	cd build && java recipejar.MainFrame -d ../Test

.PHONY: test-jar
test-jar:
	java -jar RecipeJar.jar -d Test

.PHONY: test-54
test-54:
	cd Test && java -jar RecipeJar54_177.jar

.PHONY: test-unpack
test-unpack:
	java -jar RecipeJar.jar -d test-unpack

.PHONY: test-ingredient
test-ingredient:
	cd build && java recipejar.test.testRecipe parse-ingredient

.PHONY: test-unit
test-unit:
	cd build && java recipejar.test.testRecipe convert 25 cc ml


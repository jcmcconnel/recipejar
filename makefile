# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1

SOURCEPATH=src
COMPILER=javac -sourcepath $(SOURCEPATH) -classpath build -d build
BUILDPATH=build

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


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
endif

.PHONY: test
test:
	cd build && java recipejar.MainFrame -d ../Test

.PHONY: test-jar
test-jar:
	java -jar RecipeJar.jar -d Test

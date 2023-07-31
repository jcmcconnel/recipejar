# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#
# To Do list
# ----------
#  Turn IndexFile into a singleton (?)
#  Remove RecipeFile from direct interaction with text components.
#	#  Components -> Recipe -> RecipeFile
#


SOURCEPATH=src
COMPILER=javac -sourcepath $(SOURCEPATH) -classpath build -d build

all: 
	$(COMPILER) @source_files 
	cp src/*.* build
	jar cfm RecipeJar.jar manifest -C build/ recipejar/

mainframe: 
	$(COMPILER) src/recipejar/MainFrame.java
	cp src/*.* build
	jar cfm RecipeJar.jar manifest -C build/ recipejar/

.PHONY: clean
clean:
	rm -r build

.PHONY: test
test:
	cd build && java recipejar.MainFrame -d ../Test

.PHONY: test-jar
test-jar:
	java -jar RecipeJar.jar -d Test

# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#
# To Do list
# ----------
#  Turn IndexFile into a singleton.
#  Remove RecipeFile from direct interaction with text components.
#	#  Components -> Recipe -> RecipeFile
#


SOURCEPATH=src
COMPILER=javac -Xlint:deprecation -sourcepath $(SOURCEPATH) -classpath build -d build

all: 
	$(COMPILER) @source_files 
	jar cfm RecipeJar.jar manifest -C build/ recipejar/



.PHONY: clean

clean:
	rm -r build


test:
	cd build && java recipejar.mainFrame

test-jar:
	java -jar RecipeJar.jar -d Test

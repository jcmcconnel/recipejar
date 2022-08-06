# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#
# To Do list
# ----------
#  Turn IndexFile into a singleton.
#  Remove RecipeFile from direct interaction with text components.
	#  Components -> Recipe -> RecipeFile
#


SOURCEPATH=src
COMPILER=javac -Xlint:deprecation -sourcepath $(SOURCEPATH) -classpath build -d build

all: 
	$(COMPILER) @source_files 
	jar cfm RecipeJar.jar manifest -C build/recipejar/*.class
#all: libPackage filetypes recipePackage frame

#libPackage: $(SOURCEPATH)/recipejar/lib/*.java
#	$(COMPILER) $(SOURCEPATH)/recipejar/lib/*.java
#
##Depends on recipePackage
#recipePackage: $(SOURCEPATH)/recipejar/recipe/*.java
#	$(COMPILER) $(SOURCEPATH)/recipejar/recipe/Unit.java $(SOURCEPATH)/recipejar/recipe/*.java
#
#filetypes: $(SOURCEPATH)/recipejar/filetypes/*.java
#	$(COMPILER) $(SOURCEPATH)/recipejar/filetypes/*.java
#
#editorpanel: $(SOURCEPATH)/recipejar/EditorPanel.java
#	$(COMPILER) $(SOURCEPATH)/recipejar/EditorPanel.java
#
#frame: $(SOURCEPATH)/recipejar/testFrame.java $(SOURCEPATH)/recipejar/AlphaTab.java $(SOURCEPATH)/recipejar/rjTextPane.java $(SOURCEPATH)/recipejar/EditorPanel.java
#	$(COMPILER) $(SOURCEPATH)/recipejar/testFrame.java $(SOURCEPATH)/recipejar/AlphaTab.java $(SOURCEPATH)/recipejar/rjTextPane.java $(SOURCEPATH)/recipejar/EditorPanel.java
#
#kernel: $(SOURCEPATH)/recipejar/kernel.java 
#	$(COMPILER) $(SOURCEPATH)/recipejar/kernel.java 
#



.PHONY: clean

clean:
	rm -r build


test:
	cd build && java recipejar.testFrame

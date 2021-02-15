# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#
# To Do list
# ----------
#  Add Menu bar
#  Turn IndexFile into a singleton.
#  Remove RecipeFile from direct interaction with text components.
	#  Components -> Recipe -> RecipeFile
#


COMPILER=javac -d build
all: libPackage filetypes recipePackage frame

libPackage: recipejar/lib/*.java
	$(COMPILER) recipejar/lib/*.java

#Depends on recipePackage
recipePackage: recipejar/recipe/*.java
	$(COMPILER) recipejar/recipe/Unit.java recipejar/recipe/*.java

filetypes: recipejar/filetypes/*.java
	$(COMPILER) recipejar/filetypes/*.java

editorpanel: recipejar/EditorPanel.java
	$(COMPILER) recipejar/EditorPanel.java

frame: recipejar/testFrame.java recipejar/AlphaTab.java recipejar/rjTextPane.java recipejar/EditorPanel.java
	$(COMPILER) recipejar/testFrame.java recipejar/AlphaTab.java recipejar/rjTextPane.java recipejar/EditorPanel.java

.PHONY: clean

clean:
	rm -r build


test:
	cd build && java recipejar.testFrame

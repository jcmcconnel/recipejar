# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#
COMPILER=javac -d build
project: libPackage recipePackage frame

#Rules are in order from least dependencies to most dependencies
libPackage: recipejar/lib/*.java
	$(COMPILER) recipejar/lib/*.java

#Depends on recipePackage
recipePackage: recipejar/recipe/Unit.java recipejar/recipe/*.java
	$(COMPILER) recipejar/recipe/Unit.java recipejar/recipe/*.java

filetypes: recipejar/filetypes/*.java
	$(COMPILER) recipejar/filetypes/*.java

frame: recipejar/testFrame.java recipejar/AlphaTab.java recipejar/rjTextPane.java
	$(COMPILER) recipejar/testFrame.java recipejar/AlphaTab.java recipejar/rjTextPane.java

.PHONY: clean

clean:
	rm -r build


test:
	cd build && java recipejar.testFrame

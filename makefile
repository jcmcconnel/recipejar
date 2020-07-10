# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#
project: libPackage recipePackage

#Rules are in order from least dependencies to most dependencies
libPackage: recipejar/lib/*.java
	javac recipejar/lib/*.java

#Depends on recipePackage
recipePackage: recipejar/recipe/Unit.java recipejar/recipe/*.java
	javac recipejar/recipe/Unit.java recipejar/recipe/*.java


.PHONY: clean

clean:
	rm recipejar/lib/*.class
	rm recipejar/recipe/*.class



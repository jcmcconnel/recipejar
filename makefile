unitTest: recipe/Unit.java recipe/testUnits.java
	javac recipe/Unit.java recipe/testUnits.java

.PHONY: clean

clean:
	rm recipe/*.class



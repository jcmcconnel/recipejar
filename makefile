unitTest: unit/Unit.java unit/manageUnits.java
	javac unit/Unit.java unit/manageUnits.java

.PHONY: clean

clean:
	rm unit/*.class



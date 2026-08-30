all: run

compile:
	@echo ">> Compiling JNI Stubs..."
	@mkdir -p com/zoffcc/applications/trifa
	javac com/zoffcc/applications/trifa/*.java
	@echo ">> Compiling Unit Test files..."
	javac -cp "." JniToxcoreUnitTest.java Test*.java

run: compile
	@echo ">> Running Unit Tests..."
	java -cp "." -Djava.library.path=../resources/common JniToxcoreUnitTest

clean:
	@echo ">> Cleaning up..."
	rm -rf com/zoffcc/applications/trifa/*.class
	rm -f *.class

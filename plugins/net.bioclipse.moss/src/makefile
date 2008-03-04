#-----------------------------------------------------------------------
# File    : makefile    (directory: moss)
# Contents: build molecular substructure miner (MoSS aka MoFa)
# Author  : Christian Borgelt
# History : 16.07.2002 file created
#           13.05.2006 other compilers added
#           18.07.2006 creation of a jar for the GUI version added
#           25.10.2006 creation of javadoc added
#-----------------------------------------------------------------------
all:        moss

#-----------------------------------------------------------------------
# Molecular Substructure Miner
#-----------------------------------------------------------------------
moss:   *.java
	cd ..; jikes +P +Pall moss/MoSS.java
#	cd ..; javac -Xlint moss/MoSS.java
#	cd ..; gcj --main=moss.MoSS moss/*.java -o moss/moss

#-----------------------------------------------------------------------
# Distribution Packages
#-----------------------------------------------------------------------
dist:
	$(MAKE) clean jdoc src jdclean
	$(MAKE) moss jar clean

src:
	cd ..; \
        zip -r -q moss.zip moss/{*.java,makefile,run,bench,doc} \
                           moss/data/{*.smiles,*.sln,*.list} \
                           -x moss/data/ic93.sln
jar:
	cd ..; \
	echo "Main-Class: moss.MoSS" > manifest; \
	jar cfm moss.jar manifest moss/*.class; \
	rm -f manifest

#-----------------------------------------------------------------------
# Documentation
#-----------------------------------------------------------------------
jdoc:
	cd doc/java; rm -rf *; \
        javadoc -quiet -classpath ../../.. moss

#-----------------------------------------------------------------------
# Clean up
#-----------------------------------------------------------------------
clean:
	rm -f *.class *.sub *.ids *.tmp

jdclean:
	rm -rf doc/java/*

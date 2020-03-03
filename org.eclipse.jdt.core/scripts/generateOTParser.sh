#!/bin/bash
############################################################################################################################################################
# This file is part of "Object Teams Development Tooling"-Software
#
# Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
# for its Fraunhofer Institute and Computer Architecture and Software
# Technology (FIRST), Berlin, Germany and Technical University Berlin,
# Germany.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
#
# Contributors:
# Fraunhofer FIRST - Initial API and implementation
# Technical University Berlin - Initial API and implementation
#
# Dieses Script erzeugt aus der java.g Datei alle automatisch generierbaren Dateien
# und kopiert diese an die richtigen Stellen innerhalb der aktuellen Sourcen
# Das Script benötigt den Ordner "OTDTCore"
# Das Script benötigt die Datei "objectteams.g" welche aus dem Eclipse Quellcode extrahiert werden kann.
# Zu Beginn des Vorganges wird eine Kopie aller Dateien die in den Sourcen verändert werden erstellt

############################################################################################################################################################

JIKESPG_HOME=${JIKESPG_HOME:-/usr/bin}
export JIKESPG_HOME

JAVAC=${JAVAC:-java}

if test $# -ne 2
then
	echo "Achtung: JIKESPG_HOME muss KORREKT gesetzt sein!"
	echo "Aufruf: generateOTParser.sh <grammatik> <eclipse-projekt-OTDTCore-verzeichnis>"
	echo "Bsp: generateOTParser.sh objectteams.g OTDTCore"
	echo "(im Verzeichnis des Skripts aufzurufen)."
	exit
fi

GRAMMAR=`readlink -f "$1"`
SOURCE=`readlink -f "$2"`

if test -f $JIKESPG_HOME/jikespg
then
	echo "jikespg wurde gefunden"
else
	echo "JIKESPG_HOME/jikespg konnte nicht gefunden werden"
	exit
fi

if test -f $GRAMMAR
then
	echo "Verwende Grammatik:" $GRAMMAR
else
	echo "Fehler: Grammatik" $GRAMMAR "konnte nicht gefunden werden!"
	echo "Aufruf: generateOTParser.sh <grammatik> <eclipse-projekt-OTDTCore-verzeichnis>"
	exit
fi

if test -d $SOURCE
then
	echo "Verwende Eclipse-Sourcen:" $SOURCE
else
	echo "Fehler: Eclipse-Sourcen" $SOURCE "konnten nicht gefunden werden!"
	echo "Aufruf: generateOTParser.sh <grammatik> <eclipse-projekt-OTDTCore-verzeichnis>"
	exit
fi

scriptDir=`dirname  "$0"`
pluginDir=`readlink -f ${scriptDir}/..`

echo "Initiales Aufräumen"
if test -d output
then
	rm -r output
fi

if test -d backup
then
	rm -r backup
fi

echo "Leere Verzeichnisse erzeugen"
mkdir output
mkdir backup

echo "Original Dateien vor der Anpassung aus Sourcen holen"

if test -f $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java
then
	cp  $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java                  output
else
	exit
fi

if test -f $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/TerminalTokens.java
then
	cp  $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/TerminalTokens.java          output
else
	exit
fi

if test -f $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/ParserBasicInformation.java
then
	cp  $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/ParserBasicInformation.java  output
else
	exit
fi

echo "Eine Kopie der Originaldateien die verändert werden im Ordner backup erzeugen"
cp $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java                   backup
cp $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/TerminalTokens.java           backup
cp $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/ParserBasicInformation.java   backup
cp $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/parser?.rsc                   backup
cp $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser/readableNames.props		   backup

cp $GRAMMAR output

############################################################################################################################################################
cd output
echo "Parsergenerator starten"
islalr=`$JIKESPG_HOME/jikespg $GRAMMAR | tee /tmp/jikes.log | grep grammar`

if [ "$islalr" = "This grammar is LALR(1)." ]
then
	echo "Grammatik ist LALR(1)."
else
	echo "Grammatik ist nicht korrekt!"
	exit
fi

chmod 644 *

#echo "Konstanten von javasym.java umbenennen"
cat javasym.java | sed -e s/TokenName\$eof/TokenNameEOF/g -e s/TokenName\$error/TokenNameERROR/g > javasym_tmp.java
rm javasym.java
mv javasym_tmp.java javasym.java
cd ..
############################################################################################################################################################
cd output
echo "Alles unnötige wegschneiden: javadef.java"
cat javadef.java  | awk '{
	if($0 ~ /^[ \t]*{[ \t]*$/ ){
		while((getline nextline) == 1 && nextline !~ /^[ \t]*}.*$/){
			print nextline ;
		}
		print "}" ;
	}
}
' > javadef_tmp.java

echo "Alles unnötige wegschneiden: ParserBasicInformation.java"
cat ParserBasicInformation.java  | awk '{
	if($0 ~ /^.*{[ \t]*$/ ){
		print $0;
		while((getline nextline) == 1 && nextline !~ /^[ \t]*}[ \t]*$/){
			nextline="" ;
		}
		while((getline nextline) == 1 ){
			print nextline ;
		}
	} else {
		print $0
	}
}
' > ParserBasicInformation_tmp.java

echo "Kopiere den Inhalt von javadef.java nach ParserBasicInformation.java"
cat ParserBasicInformation_tmp.java javadef_tmp.java > ParserBasicInformation.java
cd ..
############################################################################################################################################################
cd output
echo "Alles unnötige wegschneiden: JavaAction.java"
cat JavaAction.java | awk '{\
	if($0 !~ /^[ \t]*\/\/ This method is part of an automatic generation : do NOT edit-modify[ \t]*$/ ){\
		print $0\
	}\
}\
' > JavaAction_tmp.java

echo "Ersetze die Methode consumeRule gegen das Token TMP_CUT_OUT"
cat Parser.java  | awk '{
	if($0 ~ /^[ \t]*protected void consumeRule\(int act\) \{[ \t]*$/ ){
		while((getline nextline) == 1 && nextline !~ /^[ \t]*}[ \t]*$/){
			nextline="" ;
		}
		while((getline nextline) == 1 && nextline !~ /^[ \t]*}[ \t]*$/){
			nextline="" ;
		}
		print "TMP_CUT_OUT";
		while((getline nextline) == 1 ){
			print nextline ;
		}
	} else {
		print $0
	}
}
' > Parser_tmp.java

cat Parser_tmp.java  | awk '{
	if($0 ~ /^TMP_CUT_OUT$/ ){
		while((getline nextline) == 1){
			nextline="" ;
		}
	} else {
		print $0
	}
}
' > Parser_1_tmp.java

cat Parser_tmp.java  | awk '{
	if($0 ~ /^TMP_CUT_OUT$/ ){
		while((getline nextline) == 1){
			print nextline;
		}
	}
}
' > Parser_2_tmp.java

echo "Kopiere den Inhalt von JavaAction.java nach Parser.java"
cat Parser_1_tmp.java JavaAction_tmp.java Parser_2_tmp.java > Parser.java
cd ..
############################################################################################################################################################
cd output
echo "Alles unnötige wegschneiden: TerminalTokens.java"
cat TerminalTokens.java  | awk '{
	if($0 ~ /^[ \t]*public interface TerminalTokens \{[ \t]*$/ ){
		print $0
		while((getline nextline) == 1 && nextline !~ /^[ \t]*int[ \t]*$/){
			print nextline ;
		}
		print nextline ;
		while((getline nextline) == 1 && nextline !~ /^[ \t]*int[ \t]*$/){
			print nextline ;
		}
		print nextline ;
		while((getline nextline) == 1 ){
			nextline ="";
		}
	} else {
		print $0
	}
}
' > TerminalTokens_tmp.java

echo "Alles unnötige wegschneiden: javasym.java"
cat javasym.java  | awk '{
	if($0 ~ /^[ \t]*public final static int[ \t]*$/ ){
		while((getline nextline) == 1 && nextline !~ /^\}$/){
			print nextline ;
		}
		print "\n\n\t// This alias is statically inserted by generateOTParser.sh:"
		print "\tint TokenNameBINDOUT = TokenNameARROW;\n}\n" ;
	}
}
' > javasym_tmp.java


echo "Kopiere den Inhalt von javasym.java nach TerminalTokens.java"
cat TerminalTokens_tmp.java javasym_tmp.java > TerminalTokens.java
cd ..
############################################################################################################################################################
cd output

cp  ../UpdateParserFiles.java .

echo "UpdateParserFiles kompilieren"
$JAVAC -classpath ${pluginDir}/bin UpdateParserFiles.java
echo "Ressourcendateien erzeugen (*.rsc)"
java -classpath ${pluginDir}/bin:. UpdateParserFiles javadcl.java javahdr.java

cd ..

############################################################################################################################################################
echo "Alle zu aktualisierenden Classfiles entfernen (Parser/TerminalTokens/ParserBasicInformation)"
if test -f $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser/Parser.class
then
	rm $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser/Parser.class
fi

if test -f $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser/TerminalTokens.class
then
	rm $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser/TerminalTokens.class
fi

if test -f $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser/ParserBasicInformation.class
then
	rm $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser/ParserBasicInformation.class
fi

#################################################################################################################
echo "Die originalen Dateien werden in den Sourcen durch die neu generierten ersetzt"
cp output/Parser.java                 $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser
cp output/TerminalTokens.java         $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser
cp output/ParserBasicInformation.java $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser
cp output/parser*.rsc                 $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser
cp output/parser*.rsc                 $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser
cp output/readableNames.props	      $SOURCE/compiler/org/eclipse/jdt/internal/compiler/parser
cp output/readableNames.props         $SOURCE/bin/org/eclipse/jdt/internal/compiler/parser
############################################################################################################################################################
#echo "Eclipse Build ausfhren mit Optionen build -os linux -ws motif"
#cd $SOURCE
#build -os linux -ws motif
#cd ..
############################################################################################################################################################
#echo "Alle nicht mehr benötigten bzw temporären Dateien entfernen"
#rm -rf output
############################################################################################################################################################
#echo "Eclipse starten"
#cd $SOURCE
#eclipse
############################################################################################################################################################
echo "Fertig"
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo "Bitte fhren sie ein 'Refresh' im Eclipse Package Explorer aus"
echo "Da Eclipse die Abhängigkeiten zu den geänderten Interfaces sonst nicht aktualisiert"

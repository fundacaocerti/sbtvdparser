#!/bin/bash

check_java()
{
	which java > /dev/null
	if [ "$?" -ne "0" ]; then
		echo 'Java is not installed in your machine'
		echo 'it is necessary to run the program.'
		echo 'To download and install the Sun JRE you need administrative privileges:'
		sudo apt-get install sun-java6-jre
		exit 1
	fi
	echo "#!/usr/bin/env xdg-open" > SBTVD\ parser.desktop
	echo "" >>  SBTVD\ parser.desktop
	echo "[Desktop Entry]" >>  SBTVD\ parser.desktop
	echo "Type=Application" >>  SBTVD\ parser.desktop
	echo "Icon[en_US]=gnome-nettool" >>  SBTVD\ parser.desktop
	echo "Icon[pt_BR]=gnome-nettool" >>  SBTVD\ parser.desktop
	echo "Name[pt_BR]=SBTVD parser" >>  SBTVD\ parser.desktop
	echo "Name[pt_BR]=Analisador SBTVD" >>  SBTVD\ parser.desktop
	echo "Terminal=false" >>  SBTVD\ parser.desktop
	echo "Exec=" >>  SBTVD\ parser.desktop
	which java >>  SBTVD\ parser.desktop
	echo "Terminal=false" >>  SBTVD\ parser.desktop
	echo "Terminal=false" >>  SBTVD\ parser.desktop
	cat SBTVD\ parser.desktop
}

portugues_BR()
{
	lang="pt"
	check_java
}

english_US()
{
	echo 'enus'
}

abort_cancelar()
{
	echo
}

echo 'Select the installation language / Selecione o idioma de instalação:'
select choice in portugues_BR english_US abort_cancelar ;do
   $choice 
   break
done

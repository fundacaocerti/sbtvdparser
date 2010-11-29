For Linux/GTK (Ubuntu) users:
- Install libswt-gtk-3.5-java (using sinaptyc)
- Add the /usr/share/java/swt-gtk-3.5.1.jar to the project's library path
- Add the VM argument -Djava.library.path=/usr/lib/jni to the MainPanel run configuration
Now you should be able to compile and run the parser

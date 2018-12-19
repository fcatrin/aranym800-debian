# Java port of Atari800

This port is basically a library to include the Atari 800
emulaor in your own Java project. In "src" you will find a working 
Java app to test your build.

## Building the native code

Go to the main `atari800/src` directory of atari800 an run

```
./configure --target=java
make
```

This will create the file `libs/libatari800.so` which is the emulator
ready to be used as a native library from Java.

## Building the example app

In `java/src` you will find the followinf classes:

- Atari800 : Sample application
- NativeClient : The interface that the java app (client) must implement
- NativeInterface : The interface that the java app must call

## Running the sample app

java -cp bin -Djava.library.path=libs atari800.Atari800
java -cp bin -Djava.library.path=libs atari800.Atari800 /tmp/atari/ninja.atr
java -cp bin -Djava.library.path=libs atari800.Atari800 /tmp/prince.xex -nobasic


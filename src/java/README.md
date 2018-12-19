# Java port of Atari800

This port builds a library to include the Atari 800
emulator in your own Java project. In `src` you will find a working 
example Java app to test your build.

## Building the native code

Go to the main `atari800/src` directory and run

```
./configure --target=java
make
```

This will create the file `java/libs/libatari800.so` which is the emulator
ready to be used as a native library from Java.

## Building the example app

In `java/src` you will find the following classes:

- Atari800 and AtariCanvas : Sample application
- NativeClient    : The interface that the java app (client) must implement
- NativeInterface : The interface that the java app must call

## Running the sample app

Here are some examples considering that `bin` contains your compiled classes 
and `libs` contains the build library `libatari800.so`

```
java -cp bin -Djava.library.path=libs sample.Atari800
java -cp bin -Djava.library.path=libs sample.Atari800 /tmp/atari/ninja.atr
java -cp bin -Djava.library.path=libs sample.Atari800 /tmp/prince.xex -nobasic
```

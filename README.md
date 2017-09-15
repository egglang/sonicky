# sonicky
Python and Android modules for connectionless ultrasonic message transfer.

## Requirements

`sonicky` currently supports Python and Android.

Python module:

* python 3 or newer
* numpy & scipy
* reedsolo
* PyAudio

You will also need `pyautogui` in order to use slide operator example.

Android module:

* ICS or newer

## Example use cases

This library has two features;
* Encode some data and send it as ultrasonic
* Receive ultrasonic and decode it

### Start a receiver

```
$ python receiver_example.py
```

This will start a receiver and listen.


### Send text

In order to send some text, do:

```
$ python sender_example.py
```

### Example for Android 

Android encoder/decoder and an example application using it is provided.

## Developed By

* egglang - <t.egawa@gmail.com>

### Credits

 * Harvest Zhang and Bonnie Eisenman - Authors of [SqueakyChat][1], I've inspired by their contribution and trying to improve their idea.
 * Masayuki Miyazaki - Author of [Reedsolomon Java library][2].
 * Alumni in the same lab together in AIIT - Part of this activity is based on what I learned at the university.

## License

`sonicky` is Apache 2.0 Licensed. 

[1]: https://github.com/bonniee/ultrasonic/
[2]: http://sourceforge.jp/projects/reedsolomon/


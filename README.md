# sonicky
Python and Android modules for connectionless ultrasonic message transfer.

## Requirements

`sonicky` currently supports Python and Android.

Python module:

* python 3 or newer
* numpy & scipy
* reedsolo
* PyAudio

You will also need [pyautogui][1] if you want to use a slide operator example.

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


### Minimal source code to receive

The minimal source code to start a receiver is as follows.

```
from sonicky.codec.ecc import OnebyteReedSolomonEcc
from sonicky.communication import SoundReceiver

receiver = SoundReceiver(debug=True, coder=OnebyteReedSolomonEcc())
while True:
    data = receiver.receive()
    if len(data) > 0:
        data_string = receiver.convert_data_to_ascii_string(data)
        print("Decoded Decimal: %s" % [int(d) for d in data])
        print("Decoded Binary : %s" % [format(int(d), 'b') for d in data])
        print("Decoded String : %s" % data_string)

```

### Send text

In order to send some text, do:

```
$ python sender_example.py
```

### Minimal source code to send

The minimal source code to send some `Hello` is as follows.

```
from sonicky.codec.ecc import OnebyteReedSolomonEcc
from sonicky.communication import SoundSender

sender = SoundSender(debug=True, coder=OnebyteReedSolomonEcc())
sender.send_string("Hello")
```

### Example for Android 

Android encoder/decoder and an example application using it are also provided. However, while the library generally works fine, the example is not substantial yet. I am keenly preparing a better example to show various use cases.

## Developed By

* egglang - <t.egawa@gmail.com>

### Credits

 * Harvest Zhang and Bonnie Eisenman - Authors of [SqueakyChat][2], I've inspired by their contribution and trying to improve their idea.
 * Masayuki Miyazaki - Author of [Reedsolomon Java library][3].
 * Alumni in the same lab together in AIIT - Part of this activity is based on what I learned at the university.

## License

`sonicky` is Apache 2.0 Licensed. 

[1]: https://github.com/asweigart/pyautogui
[2]: https://github.com/bonniee/ultrasonic/
[3]: http://sourceforge.jp/projects/reedsolomon/


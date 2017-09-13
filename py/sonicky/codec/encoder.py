# -*- coding: utf-8 -*-
#
# Copyright 2017 egglang <t.egawa@gmail.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse

import numpy as np
import pyaudio
from scipy.io import wavfile

from sonicky.codec.conf import Settings
from sonicky.codec.ecc import EmptyEcc


class Encoder:
    def __init__(self, debug=False, coder=EmptyEcc(), setting=Settings()):
        self.pyaudio = pyaudio.PyAudio()
        self.stream = self.pyaudio.open(format=pyaudio.paInt16,
                                        channels=1,
                                        rate=setting.rate,
                                        output=True,
                                        frames_per_buffer=setting.audiobuf_size)
        self.debug = debug
        self.coder = coder
        self.setting = setting

    def string2sound(self, somestring):
        binform = ''.join('-001' + self.coder.get_encoded_bytes_string(i) for i in somestring) + '-010'  #
        if self.debug:
            print("binform:" + binform)
        # After converting it to binary number, divide it by 2 bits and convert each to decimal number
        multiple = [int(binform[i:i + 4], 2) for i in range(len(binform)) if i % 4 == 0]  #
        if self.debug:
            print("multiple:" + str(multiple))
        return np.hstack([self.getbit(self.setting.char_freq[i + 2]) for i in multiple])

    def encode2wav(self, somestring, filename):
        soundlist = self.string2sound(somestring)
        # print("data:" + str(soundlist.astype(np.dtype('int16'))))
        wavfile.write(filename, self.setting.rate, soundlist.astype(np.dtype('int16')))

    def encodeplay(self, somestring):
        soundlist = self.string2sound(somestring)
        data = soundlist.astype(np.dtype('int16'))
        data_to_send = data.tobytes()
        stream = self.stream
        print("channels:" + str(stream._channels))
        print("rate:" + str(stream._rate))
        print("format:" + str(stream._format))
        print("frames_per_buffer:" + str(stream._frames_per_buffer))
        self.stream.write(data_to_send)

    def getbit(self, freq):
        music = []
        t = np.arange(0, self.setting.bit_duration, 1. / self.setting.rate)  # time
        x = np.sin(2 * np.pi * freq * t)  # generated signals
        x = [int(val * 32000) for val in x]

        sigmoid = [1 / (1 + np.power(np.e, -t)) for t in np.arange(-6, 6, 0.02)]  # 0.01
        sigmoid_inv = sigmoid[::-1]

        xstart = len(x) - len(sigmoid)
        for i in range(len(sigmoid)):
            x[xstart + i] *= sigmoid_inv[i]
            x[i] *= sigmoid[i]

        music = np.hstack((music, x))
        return music

    def quit(self):
        self.stream.stop_stream()
        self.stream.close()
        self.pyaudio.terminate()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog="sound_encoder")
    parser.add_argument('text', help="The text to encode")
    parser.add_argument('filename', help="The file to generate.")
    args = parser.parse_args()
    enc = Encoder()
    enc.encodeplay(args.text)

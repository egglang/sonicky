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

from __future__ import division
from collections import deque
import struct
import math
import sys

import pyaudio
import numpy as np

from sonicky.codec.conf import Settings
from sonicky.codec.ecc import EmptyEcc


class Decoder:
    def __init__(self, debug=True, coder=EmptyEcc(), setting=Settings()):
        self.win_len = 2 * int(setting.bit_duration * setting.rate / setting.chunk_size / 2)
        self.win_fudge = int(self.win_len / 2)
        self.buffer = deque()
        self.buf_len = self.win_len + self.win_fudge
        self.byte = []
        self.receivebytes = bytearray()
        self.idlecount = 0
        self.coder = coder
        self.do_quit = False
        self.character_callback = None
        self.idle_callback = None
        self.debug = debug
        self.setting = setting
        self.p = pyaudio.PyAudio()
        self.stream = self.p.open(format=pyaudio.paInt16,
                                  channels=1,
                                  rate=setting.rate,
                                  input=True,
                                  frames_per_buffer=setting.audiobuf_size)

    def listen(self):
        self.do_quit = False
        self.receivebytes = []

        while not self.do_quit:
            audiostr = self.stream.read(self.setting.chunk_size)
            self.audio = list(struct.unpack("%dh" % self.setting.chunk_size, audiostr))
            self.window()
            powerlist = np.array([self.goertzel(i) for i in self.setting.char_freq])
            base = self.goertzel(self.setting.baseline)
            self.update_state(powerlist, base)
            self.signal_to_bits()
            self.process_byte()
        return self.receivebytes

    def window(self):
        self.audio = [aud * win for aud, win in zip(self.audio, self.setting.window)]

    def goertzel(self, frequency):
        prev1 = prev2 = 0.0
        norm_freq = frequency / self.setting.rate
        coeff = 2 * math.cos(2 * math.pi * norm_freq)
        for sample in self.audio:
            s = sample + (coeff * prev1) - prev2
            prev2 = prev1
            prev1 = s
        power = (prev2 * prev2) + (prev1 * prev1) - (coeff * prev1 * prev2)
        return int(power) + 1  # prevents division by zero problems

    def update_state(self, powerlist, base):
        state = -3  # silent
        pw = powerlist / base
        th = np.array(self.setting.char_threshold)
        judge = pw > th
        pw[judge == False] = 0

        # Find the maximum value
        if sum(judge) > 0:
            state = int(np.argmax(pw) - 2)

        if len(self.buffer) >= self.buf_len:
            self.buffer.popleft()
        self.buffer.append(state)

    # Takes the raw noisy samples of -1/0/1 and finds the bitstream from it
    def signal_to_bits(self):
        if len(self.buffer) < self.buf_len:
            return

        buf = list(self.buffer)

        if self.debug:
            self.printbuf(buf)

        costs = [[] for i in range(19)]  #
        for i in range(self.win_fudge):
            win = buf[i: self.win_len + i]
            #
            costs[0].append(sum(x != 0 for x in win))
            costs[1].append(sum(x != 1 for x in win))
            costs[2].append(sum(x != 2 for x in win))
            costs[3].append(sum(x != 3 for x in win))
            costs[4].append(sum(x != 4 for x in win))
            costs[5].append(sum(x != 5 for x in win))
            costs[6].append(sum(x != 6 for x in win))
            costs[7].append(sum(x != 7 for x in win))
            costs[8].append(sum(x != 8 for x in win))
            costs[9].append(sum(x != 9 for x in win))
            costs[10].append(sum(x != 10 for x in win))
            costs[11].append(sum(x != 11 for x in win))
            costs[12].append(sum(x != 12 for x in win))
            costs[13].append(sum(x != 13 for x in win))
            costs[14].append(sum(x != 14 for x in win))
            costs[15].append(sum(x != 15 for x in win))
            costs[16].append(sum(x != -1 for x in win))
            costs[17].append(sum(x != -2 for x in win))
            costs[18].append(sum(x != -3 for x in win))
            #
        min_costs = [min(costs[i]) for i in range(19)]  #
        min_cost = min(min_costs)
        signal = min_costs.index(min_cost)
        fudge = costs[signal].index(min_cost)
        for i in range(self.win_len + fudge):
            self.buffer.popleft()

        # got a signal
        if signal < 16:  #
            self.byte.append(signal)

        # got charstart signal.
        elif signal == 16:  #
            signal = 's'
            self.byte = []

        # got charend signal
        elif signal == 17:  #
            signal = 'e'
            self.byte = []
            self.buffer = deque()
            self.quit()

        # If we get no signal, increment idlecount
        if signal == 18:  #
            self.idlecount += 1
        else:
            self.idlecount = 0
        if self.idlecount > self.setting.idle_limit and self.idle_callback:
            self.idlecount = 0
            self.idle_callback()

        if self.debug:
            if signal == 18:  #
                signal = '-'
            sys.stdout.write('')
            sys.stdout.write('|{}|\n'.format(signal))
            sys.stdout.flush()

    def process_byte(self):
        if len(self.byte) != self.coder.expected_size():
            return
        byte = self.coder.get_decoded_bytes(self.byte)
        if byte is not None:
            self.receivebytes.append(byte)  #
        self.byte = []

    def quit(self):
        self.do_quit = True

    def printbuf(self, buf):
        newbuf = ['-' if x is -3 else x for x in buf]  #
        newbuf = ['s' if x is -1 else x for x in newbuf]  #
        newbuf = ['e' if x is -2 else x for x in newbuf]  #
        print(repr(newbuf).replace(', ', ' ').replace('\'', ''))


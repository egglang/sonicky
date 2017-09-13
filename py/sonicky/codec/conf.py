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

__author__ = 'egg'

import numpy as np


class Settings(object):
    """ Invariable setting class """

    def __init__(self):
        self.rate = 44100
        self.chunk_size = 512
        self.window = np.hamming(self.chunk_size)
        self.audiobuf_size = 2048
        self.baseline = 17000.0
        self.char_freq = [17300.0,  # end
                          17500.0,  # start
                          17750.0,  # 0
                          17900.0,  # 1
                          18050.0,  # 2
                          18200.0,  # 3
                          18350.0,  # 4
                          18500.0,  # 5
                          18650.0,  # 6
                          18800.0,  # 7
                          18950.0,  # 8
                          19100.0,  # 9
                          19250.0,  # 10
                          19400.0,  # 11
                          19550.0,  # 12
                          19700.0,  # 13
                          19850.0,  # 14
                          20000.0]  # 15
        self.char_threshold = [20,  # end
                               20,  # start
                               80,  # 0
                               80,  # 1
                               80,  # 2
                               80,  # 3
                               80,  # 4
                               40,  # 5
                               40,  # 6
                               40,  # 7
                               40,  # 8
                               40,  # 9
                               20,  # 10
                               20,  # 11
                               20,  # 12
                               20,  # 13
                               20,  # 14
                               20]  # 15
        self.bit_duration = 0.05
        self.idle_limit = 2

    @property
    def rate(self):
        return self._rate

    @rate.setter
    def rate(self, value):
        self._rate = value

    @property
    def chunk_size(self):
        return self._chunk_size

    @chunk_size.setter
    def chunk_size(self, value):
        self._chunk_size = value

    @property
    def window(self):
        return self._window

    @window.setter
    def window(self, value):
        self._window = value

    @property
    def audiobuf_size(self):
        return self._audiobuf_size

    @audiobuf_size.setter
    def audiobuf_size(self, value):
        self._audiobuf_size = value

    @property
    def baseline(self):
        return self._baseline

    @baseline.setter
    def baseline(self, value):
        self._baseline = value

    @property
    def char_freq(self):
        return self._char_freq

    @char_freq.setter
    def char_freq(self, value):
        self._char_freq = value

    @property
    def char_threshold(self):
        return self._char_threshold

    @char_threshold.setter
    def char_threshold(self, value):
        self._char_threshold = value

    @property
    def bit_duration(self):
        return self._bit_duration

    @bit_duration.setter
    def bit_duration(self, value):
        self._bit_duration = value

    @property
    def idle_limit(self):
        """clear buffer when silent"""
        return self._idle_limit

    @idle_limit.setter
    def idle_limit(self, value):
        self._idle_limit = value

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

import reedsolo


class Ecc(object):
    """Abstract class for Error Correction Code"""

    def __init__(self, nsym):
        self.nsym = nsym
        self.symbol_len_per_byte = 2

    def expected_size(self):
        u"""Returns the size of processing unit """
        return self.nsym * self.symbol_len_per_byte

    def get_encoded_bytes_string(self, target):
        u"""Return encoded byte string"""
        pass

    def get_decoded_bytes(self, target):
        u"""Return decoded byte string"""
        pass


class EmptyEcc(Ecc):
    """ Class not doing anything """

    def __init__(self):
        Ecc.__init__(self, 1)

    def get_encoded_bytes_string(self, target):
        a = format(int(target), 'b').zfill(8)
        return a

    def get_decoded_bytes(self, target):
        ascii = int(''.join([format(i, 'b').zfill(4) for i in target]), 2)  #
        return ascii


class ReedSolomonEcc(Ecc):
    """ ReedSolomon Code """

    def __init__(self, nsym):
        Ecc.__init__(self, nsym)
        self.coder = reedsolo.RSCodec(self.nsym)

    def expected_size(self):
        return (self.nsym + 1) * self.symbol_len_per_byte

    def encode(self, data):
        return self.coder.encode(data)

    def decode(self, data):
        return self.coder.decode(data)


class OnebyteReedSolomonEcc(ReedSolomonEcc):
    """ ReedSolomon Code for 1 byte """

    def __init__(self):
        ReedSolomonEcc.__init__(self, 2)

    def get_encoded_bytes_string(self, target):
        encoded = self.encode([target])
        s = ''
        for e in encoded:
            s = s + format(int(e), 'b').zfill(8)
        return s

    def decode(self, data):
        try:
            return self.coder.decode(data)
        except reedsolo.ReedSolomonError as e:
            return bytearray()  # empty bytearray

    def get_decoded_bytes(self, target):
        paired = [target[i:i + 2] for i in range(0, len(target), 2)]
        sl = [((en[0] << 4) + en[1]) for en in paired]
        decoded = self.decode(sl)
        return int(decoded[0]) if len(decoded) > 0 else None

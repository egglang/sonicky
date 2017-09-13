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

from sonicky.codec.ecc import OnebyteReedSolomonEcc, EmptyEcc
from sonicky.communication import SoundReceiver

__author__ = 'egg'


def launch_receiver(coder=EmptyEcc()):
    print("Listening...")
    receiver = SoundReceiver(debug=False, coder=coder)
    while True:
        data = receiver.receive()
        if len(data) > 0:
            data_string = receiver.convert_data_to_ascii_string(data)
            print("Decoded Decimal: %s" % [int(d) for d in data])
            print("Decoded Binary : %s" % [format(int(d), 'b') for d in data])
            print("Decoded String : %s" % data_string)


if __name__ == "__main__":
    # Launch a sound receiver with Reedsolomon
    launch_receiver(OnebyteReedSolomonEcc())
    # Launch a sound receiver without error correcting
    # launch_receiver()

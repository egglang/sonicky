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
from sonicky.communication import SoundWriter, SoundSender

__author__ = 'egg'


def write(coder=EmptyEcc(), debug=False, target_str='', file_name='./out.wav'):
    print("Encoding...")
    writer = SoundWriter(debug=debug, coder=coder)
    writer.write_string_to_file(target_str, file_name)


def send(coder=EmptyEcc(), debug=False, target_str=''):
    print("Encoding...")
    sender = SoundSender(debug=debug, coder=coder)
    sender.send_string(target_str)


if __name__ == "__main__":
    # send string message with Reedsolomon
    send(coder=OnebyteReedSolomonEcc(), debug=True, target_str="Hello!")
    # write(coder=OnebyteReedSolomonEcc(), target_str="nn", file_name="out.wav")

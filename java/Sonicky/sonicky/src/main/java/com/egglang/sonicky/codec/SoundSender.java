/*
 * Copyright 2017 egglang <t.egawa@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.egglang.sonicky.codec;


import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SoundSender {
    private final Encoder mEncoder;

    public SoundSender() {
        this(true);
    }

    public SoundSender(boolean eccEnabled) {
        mEncoder = new Encoder(eccEnabled);
    }

    public void sendByteData(byte[] data) {
        sendDataImpl(data);
    }

    public void sendString(String s) {
        try {
            byte[] dataToSend = createBytes(s);
            sendByteData(dataToSend);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private byte[] createBytes(String s) throws UnsupportedEncodingException {
        String d = String.format("%s\n", s);
        return d.getBytes("UTF-8");
    }

    private void sendDataImpl(byte[] data) {
//        Log.d(TAG, String.format("Data: %s", Arrays.toString(data)));
//        print("Decimalize: %s" % [int(d) for d in data])
//        print("Binarize: %s" %  [format(int(d), 'b') for d in data])
        mEncoder.encodePlay(data);

    }

    public void quit() {
        mEncoder.quit();
    }
}

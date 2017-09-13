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

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SoundReceiver {

    private Decoder mDecoder;

    public SoundReceiver() {
        this(true);
    }

    public SoundReceiver(boolean eccEnabled) {
        mDecoder = new Decoder(eccEnabled);
    }

    public byte[] receive() {
        byte[] bytes = mDecoder.listen();
        if (bytes != null && bytes.length > 0) {
            return bytes;//parseBody(bytes);
        }
        return null;
    }

    public String receiveAsString() {
        byte[] bytes = receive();
        if (bytes != null && bytes.length > 0) {
            try {
                return parseReceiveData(bytes);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

//    private byte[] parseBody(byte[] data) {
//        byte[] ret = new byte[data.length - 1];
//        System.arraycopy(data, 1, ret, 0, ret.length);
//        return ret;
//    }

    public String parseReceiveData(byte[] data) throws UnsupportedEncodingException {
        Log.d(getClass().getName(), String.format("receive binary: %s", Arrays.toString(data)));
        String string = new String(data, "UTF-8");
        Log.d(getClass().getName(), String.format("receive string: %s", string));
        return string;
    }

    public void quit() {
        mDecoder.quit();
    }
}

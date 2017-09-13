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

package com.egglang.sonicky.ecc;

import android.util.Log;

import jp.sourceforge.reedsolomon.RsEncode;

public class ReedSolomonEccEncoder implements EccEncoder {
    RsEncode enc = new RsEncode(2);

    @Override
    public byte[] getEncodedBytes(byte b) {
        int[] input = new int[] { b };
        int[] parity = new int[2];
        int status = enc.encode(input, parity);
        if (status < 0) {
            Log.d("EccEncoder", "Unable to encode.");
            throw new IllegalArgumentException("Unable to encode.");
        }
        return new byte[] {b, (byte) parity[0], (byte) parity[1]};
    }
}
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

import jp.sourceforge.reedsolomon.RsDecode;

public class ReedSolomonEccDecoder implements EccDecoder {
    RsDecode mRsDecode = new RsDecode(2);

    @Override
    public byte[] decode(int[] origin) {
        try {
            int status = mRsDecode.decode(origin);
            if (status < 0) {
                Log.d("EccDecoder", "Unable to decode.");
                return new byte[0];
            }
            byte result = (byte) origin[0];
            return new byte[]{result};
        } catch (RuntimeException e) {
            Log.e("EccDecoder", "Unable to decode.", e);
            return new byte[0];
        }
    }

    @Override
    public int getLen() {
        return 6;
    }
}
